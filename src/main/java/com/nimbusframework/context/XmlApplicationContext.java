/*
 * Copyright (C) 2026 neossoftware
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * @author neossoftware
 */
package com.nimbusframework.context;

import com.nimbusframework.annotation.Autowired;
import com.nimbusframework.annotation.BeanScope;
import com.nimbusframework.annotation.Component;
import com.nimbusframework.annotation.PersistenceContext;
import com.nimbusframework.annotation.Qualifier;
import com.nimbusframework.annotation.Scope;
import com.nimbusframework.annotation.Transactional;
import com.nimbusframework.annotation.Value;
import com.nimbusframework.bind.TypeConverter;
import com.nimbusframework.config.PropertiesLoader;
import com.nimbusframework.jpa.EntityManagerProxyFactory;
import com.nimbusframework.jpa.TransactionalProxyFactory;
import com.nimbusframework.repository.JpaRepository;
import com.nimbusframework.repository.RepositoryProxyFactory;
import com.nimbusframework.scan.ClassPathScanner;
import com.nimbusframework.view.JspViewResolver;
import com.nimbusframework.view.ViewResolver;
import com.nimbusframework.web.HandlerInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

/**
 * Implementación del ApplicationContext basada en XML.
 *
 * Orden de inicialización:
 *   1. parseProperties       — carga .properties; debe ser PRIMERO para que @Value funcione
 *   2. parseViewResolver
 *   3. parseBeans            — beans explícitos en XML (siempre singleton)
 *   4. parseComponentScan    — REGISTRA clases @Component/prototype (no instancia todavía)
 *   5. parseJpa
 *   6. createRepositoryProxies
 *   7. instantiateComponents — instancia los singletons registrados en (4), resolviendo
 *                              {@code @Autowired} de constructor (con {@code @Qualifier}) recursivamente;
 *                              el orden de declaración/escaneo ya no importa
 *   8. injectPersistenceContext
 *   9. wrapTransactionalBeans
 *  10. [auto-registro como bean "applicationContext"]
 *  11. injectDependencies    — @Autowired + @Value + @Qualifier POR CAMPO en todos los singletons
 *  12. parseInterceptors     — instancia interceptores e inyecta en ellos @Autowired + @Value
 *
 * Inyección por constructor: una clase puede declarar UN constructor @Autowired;
 * sus parámetros se resuelven por tipo, o por nombre de bean si llevan @Qualifier.
 * Nota: si esa dependencia es @Transactional, el constructor recibe la instancia
 * SIN el proxy transaccional (wrapTransactionalBeans corre después, paso 9) —
 * la inyección por campo (paso 11) sí recibe el proxy ya envuelto.
 *
 * {@code <import resource="...">} — antes de correr las fases 1-5, se resuelve
 * recursivamente el árbol de imports (mismas convenciones que {@code <properties
 * file="...">}: "classpath:", "file:", o classpath por defecto). Cada fase corre
 * sobre TODOS los documentos (imports primero, en profundidad; el raíz al final),
 * así un documento puede referenciar (component-scan, {@code <bean ref="...">}) algo
 * definido en un import, pero no al revés. Se detectan ciclos entre imports.
 * <pre>
 *   {@code <import resource="application/config/myconfig.xml"/>}
 * </pre>
 *
 * {@code <bean>} soporta {@code <property>} hijos para setter injection (convención
 * JavaBean: {@code name="foo"} busca {@code setFoo(...)}), con valor literal o
 * referencia a otro bean:
 * <pre>
 *   {@code <bean id="mybean" class="com.MyClase">}
 *     {@code <property name="nombre" value="datos"/>}
 *     {@code <property name="otroBean" ref="otroBeanId"/>}
 *   {@code </bean>}
 * </pre>
 */
public class XmlApplicationContext implements ApplicationContext {

    private static final Logger log = Logger.getLogger(XmlApplicationContext.class.getName());

    // ── Singletons ──────────────────────────────────────────────────────────
    private final Map<String, Object>  beans     = new LinkedHashMap<>();
    private final Map<String, Object>  originals = new LinkedHashMap<>();

    // ── Prototypes ───────────────────────────────────────────────────────────
    private final Map<String, Class<?>> prototypeRecipes = new LinkedHashMap<>();

    // ── Componentes registrados por component-scan, pendientes de instanciar ──
    // (permite resolver @Autowired de constructor sin importar el orden de escaneo)
    private final Map<String, Class<?>> componentClasses = new LinkedHashMap<>();
    private final Set<String>           inProgress       = new HashSet<>();

    // ── Properties (@Value) ──────────────────────────────────────────────────
    private final Map<String, String> properties = new LinkedHashMap<>();

    // ── Interceptores ────────────────────────────────────────────────────────
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();

    // ── JPA ──────────────────────────────────────────────────────────────────
    private final List<Class<?>>   pendingRepositoryInterfaces = new ArrayList<>();
    private ViewResolver           viewResolver;
    private EntityManagerFactory   entityManagerFactory;
    private EntityManager          sharedEmProxy;

    // ── ClassLoader para PropertiesLoader ────────────────────────────────────
    private ClassLoader contextClassLoader;

    // ── Init ─────────────────────────────────────────────────────────────────

    /**
     * Parsea {@code configStream} (framework-config.xml, resolviendo {@code <import>} recursivamente)
     * e inicializa el contenedor completo en orden: properties, view-resolver, beans, component-scan,
     * JPA, instanciación de componentes, inyección de dependencias e interceptores.
     *
     * @throws RuntimeException si falla el parseo o cualquier fase de la inicialización.
     */
    public XmlApplicationContext(InputStream configStream) {
        log.info("Inicializando ApplicationContext...");
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Document rootDoc = parseXmlDocument(configStream);

            // Resuelve <import resource="..."/> recursivamente (profundidad primero):
            // cada documento importado queda ANTES que quien lo importa, así una
            // definición del doc raíz puede referenciar (<property ref="...">) un
            // bean definido en un import, pero no al revés.
            List<Document> allDocs = new ArrayList<>();
            collectDocuments(rootDoc, allDocs, new HashSet<>());

            for (Document d : allDocs) parseProperties(d);        // 1 — PRIMERO: @Value depende de esto
            for (Document d : allDocs) parseViewResolver(d);      // 2
            for (Document d : allDocs) parseBeans(d);              // 3
            for (Document d : allDocs) parseComponentScan(d);      // 4 — registra clases, no instancia
            for (Document d : allDocs) parseJpa(d);                // 5
            createRepositoryProxies();     // 6
            instantiateComponents();       // 7 — instancia con soporte de @Autowired de constructor
            injectPersistenceContext();    // 8
            wrapTransactionalBeans();      // 9
            beans.put("applicationContext", this);    // 10
            originals.put("applicationContext", this);
            injectDependencies();          // 11 — @Autowired + @Value + @Qualifier por CAMPO
            for (Document d : allDocs) parseInterceptors(d);       // 12 — después de injectDependencies

        } catch (Exception e) {
            throw new RuntimeException("Fallo al inicializar ApplicationContext", e);
        }
        log.info("ApplicationContext listo."
            + " Singletons=" + beans.keySet()
            + " Prototypes=" + prototypeRecipes.keySet()
            + " Properties=" + properties.size()
            + " Interceptores=" + interceptors.size());
    }

    private Document parseXmlDocument(InputStream configStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(configStream);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Resuelve {@code <import resource="...">} de forma recursiva y en profundidad:
     * los imports de {@code doc} se agregan a {@code out} ANTES que {@code doc} mismo
     * (y los imports anidados de cada import, antes que ese import), para que las
     * fases de parseo (properties/beans/component-scan/jpa) puedan procesarse en ese
     * mismo orden y una definición pueda referenciar algo definido "más abajo" en la
     * cadena de imports.
     */
    private void collectDocuments(Document doc, List<Document> out, Set<String> visitedResources) throws Exception {
        NodeList importNodes = doc.getElementsByTagName("import");
        for (int i = 0; i < importNodes.getLength(); i++) {
            Element el       = (Element) importNodes.item(i);
            String  resource = el.getAttribute("resource");
            if (resource == null || resource.isEmpty()) continue;

            if (!visitedResources.add(resource)) {
                log.warning("<import resource=\"" + resource + "\"> ya fue procesado — se omite (¿ciclo?).");
                continue;
            }
            Document importedDoc;
            try (InputStream is = openResource(resource)) {
                if (is == null) {
                    throw new RuntimeException("<import>: recurso no encontrado en el classpath: " + resource);
                }
                importedDoc = parseXmlDocument(is);
                log.info("<import resource=\"" + resource + "\"> cargado");
            }
            collectDocuments(importedDoc, out, visitedResources);
        }
        out.add(doc);
    }

    /** Igual convención que {@code <properties file="...">}: "classpath:", "file:", o classpath por defecto. */
    private InputStream openResource(String resource) throws java.io.IOException {
        String path = resource;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        } else if (path.startsWith("file:")) {
            return new java.io.FileInputStream(path.substring("file:".length()));
        }
        InputStream is = contextClassLoader.getResourceAsStream(path);
        return (is != null) ? is : contextClassLoader.getResourceAsStream("/" + path);
    }

    // -----------------------------------------------------------------------
    // Parsers
    // -----------------------------------------------------------------------

    private void parseProperties(Document doc) {
        NodeList nodes = doc.getElementsByTagName("properties");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element  el       = (Element) nodes.item(i);
            String   file     = el.getAttribute("file");
            try {
                java.util.Properties props = PropertiesLoader.load(file, contextClassLoader);
                for (String key : props.stringPropertyNames()) {
                    properties.put(key, props.getProperty(key));
                }
                log.info("<properties file=\"" + file + "\"> cargado: " + props.size() + " entradas");
            } catch (Exception e) {
                log.warning("No se pudo cargar properties: " + file + " — " + e.getMessage());
            }
        }
    }

    private void parseViewResolver(Document doc) {
        NodeList nodes = doc.getElementsByTagName("view-resolver");
        if (nodes.getLength() > 0) {
            Element el     = (Element) nodes.item(0);
            String  prefix = el.getAttribute("prefix");
            String  suffix = el.getAttribute("suffix");
            viewResolver   = new JspViewResolver(prefix, suffix);
        } else {
            viewResolver = new JspViewResolver("/WEB-INF/views/", ".jsp");
        }
    }

    private void parseBeans(Document doc) throws Exception {
        NodeList beanNodes = doc.getElementsByTagName("bean");
        for (int i = 0; i < beanNodes.getLength(); i++) {
            Element  el        = (Element) beanNodes.item(i);
            String   id        = el.getAttribute("id");
            String   className = el.getAttribute("class");
            Class<?> clazz     = Class.forName(className);
            Object   instance  = createInstance(clazz);
            addSingleton(id, instance);
            applyProperties(el, instance, id);
            log.info("Bean XML: id=" + id + " clase=" + className);
        }
    }

    /**
     * Procesa los {@code <property>} hijos DIRECTOS de un {@code <bean>}: por setter
     * (convención JavaBean: {@code name="foo"} → {@code setFoo(...)}), con valor literal
     * ({@code value="..."} o {@code <value>...</value>} anidado, ambos soportan ${...})
     * o {@code ref} (referencia a otro bean por id/nombre).
     *
     * Nota: {@code ref} solo resuelve beans XML ya creados en un import previo o
     * declarados ANTES en el mismo documento (o componentes @Component/@Service) —
     * no soporta referencias hacia adelante entre <bean> del mismo archivo.
     */
    private void applyProperties(Element beanEl, Object instance, String beanId) {
        NodeList children = beanEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element)) continue;
            Element child = (Element) children.item(i);
            if (!"property".equals(child.getTagName())) continue;

            String propertyName = child.getAttribute("name");
            Method setter       = findSetter(instance.getClass(), propertyName);
            if (setter == null) {
                log.warning("<property name=\"" + propertyName + "\">: no se encontró setter en "
                    + instance.getClass().getName() + " (bean '" + beanId + "')");
                continue;
            }

            Object value;
            if (child.hasAttribute("ref")) {
                String refName = child.getAttribute("ref");
                value = getBeanEnsureCreated(refName);
                if (value == null) {
                    throw new RuntimeException("<property name=\"" + propertyName + "\" ref=\"" + refName
                        + "\">: no se encontró el bean referenciado (bean '" + beanId + "')");
                }
            } else {
                String literal  = readLiteralValue(child);
                String resolved = resolveExpression(literal);
                value = TypeConverter.convert(resolved, setter.getParameterTypes()[0]);
            }

            try {
                setter.setAccessible(true);
                setter.invoke(instance, value);
                log.fine("<property>: " + instance.getClass().getSimpleName() + "." + propertyName
                    + " = " + (child.hasAttribute("ref") ? "ref:" + child.getAttribute("ref") : value));
            } catch (Exception e) {
                throw new RuntimeException("No se pudo asignar <property name=\"" + propertyName
                    + "\"> en el bean '" + beanId + "'", e);
            }
        }
    }

    /** {@code <property value="X">} o, si no está el atributo, {@code <property><value>X</value></property>}. */
    private static String readLiteralValue(Element propertyEl) {
        if (propertyEl.hasAttribute("value")) {
            return propertyEl.getAttribute("value");
        }
        NodeList valueTags = propertyEl.getElementsByTagName("value");
        if (valueTags.getLength() > 0) {
            return valueTags.item(0).getTextContent().trim();
        }
        return null;
    }

    private static Method findSetter(Class<?> clazz, String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) return null;
        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) return m;
        }
        return null;
    }

    /**
     * Escanea y REGISTRA las clases @Component/@Service/etc. y prototypes,
     * pero todavía no las instancia — eso lo hace {@link #instantiateComponents()}
     * después de que JPA y los repository proxies estén listos, para que la
     * inyección por constructor pueda resolver cualquier dependencia sin
     * importar el orden de escaneo.
     */
    private void parseComponentScan(Document doc) {
        NodeList nodes = doc.getElementsByTagName("component-scan");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el          = (Element) nodes.item(i);
            String  basePackage = el.getAttribute("base-package");
            log.info("Escaneando: " + basePackage);
            List<Class<?>> found = ClassPathScanner.scan(basePackage);

            for (Class<?> clazz : found) {
                if (clazz.isInterface() && isJpaRepositoryInterface(clazz)) {
                    pendingRepositoryInterfaces.add(clazz);
                    continue;
                }
                if (clazz.isInterface() || !isComponentAnnotated(clazz)) continue;

                String beanName = toBeanName(clazz.getSimpleName());
                if (beans.containsKey(beanName) || prototypeRecipes.containsKey(beanName)
                        || componentClasses.containsKey(beanName)) continue;

                if (isPrototypeScope(clazz)) {
                    prototypeRecipes.put(beanName, clazz);
                    log.info("@Scope(prototype): " + beanName);
                } else {
                    componentClasses.put(beanName, clazz);
                }
            }
        }
    }

    /** Instancia todos los singletons registrados por {@link #parseComponentScan}. */
    private void instantiateComponents() {
        for (String beanName : new ArrayList<>(componentClasses.keySet())) {
            createSingletonIfAbsent(beanName);
        }
    }

    /**
     * Crea (si hace falta) el singleton registrado bajo {@code beanName}, resolviendo
     * su constructor @Autowired de forma recursiva. Detecta dependencias circulares.
     */
    private Object createSingletonIfAbsent(String beanName) {
        Object existing = beans.get(beanName);
        if (existing != null) return existing;

        Class<?> clazz = componentClasses.get(beanName);
        if (clazz == null) return null;

        if (inProgress.contains(beanName)) {
            throw new RuntimeException("Dependencia circular detectada creando el bean '" + beanName
                + "' (" + clazz.getName() + "). Nimbus no soporta ciclos de inyección por constructor.");
        }
        inProgress.add(beanName);
        try {
            Object instance = createInstance(clazz);
            addSingleton(beanName, instance);
            log.info("@Component: " + beanName);
            return instance;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el bean '" + beanName + "' (" + clazz.getName() + ")", e);
        } finally {
            inProgress.remove(beanName);
        }
    }

    private void parseJpa(Document doc) {
        NodeList nodes = doc.getElementsByTagName("jpa");
        if (nodes.getLength() == 0) return;
        Element jpaEl  = (Element) nodes.item(0);
        String  puName = jpaEl.getAttribute("persistence-unit");

        Map<String, Object> props = new HashMap<>();
        NodeList propNodes = jpaEl.getElementsByTagName("property");
        for (int i = 0; i < propNodes.getLength(); i++) {
            Element propEl = (Element) propNodes.item(i);
            // Soporta ${key} en los valores JPA también
            String rawValue = propEl.getAttribute("value");
            props.put(propEl.getAttribute("name"), resolveExpression(rawValue));
        }
        log.info("Creando EntityManagerFactory: " + puName);
        entityManagerFactory = Persistence.createEntityManagerFactory(puName, props);
    }

    private void parseInterceptors(Document doc) throws Exception {
        NodeList interceptorsNodes = doc.getElementsByTagName("interceptors");
        if (interceptorsNodes.getLength() == 0) return;
        Element  interceptorsEl = (Element) interceptorsNodes.item(0);
        NodeList items          = interceptorsEl.getElementsByTagName("interceptor");

        for (int i = 0; i < items.getLength(); i++) {
            Element  el        = (Element) items.item(i);
            String   className = el.getAttribute("class");
            Class<?> clazz     = Class.forName(className);
            Object   instance  = createInstance(clazz);
            injectFieldsInto(instance);    // @Autowired + @Value en el interceptor
            interceptors.add((HandlerInterceptor) instance);
            log.info("Interceptor registrado: " + className);
        }
    }

    // -----------------------------------------------------------------------
    // Inyección — singletons
    // -----------------------------------------------------------------------

    private void injectPersistenceContext() throws IllegalAccessException {
        if (entityManagerFactory == null) return;
        sharedEmProxy = EntityManagerProxyFactory.createProxy(entityManagerFactory);
        for (Object bean : originals.values()) {
            injectEmInto(bean);
        }
    }

    private void wrapTransactionalBeans() {
        if (entityManagerFactory == null) return;
        for (Map.Entry<String, Object> entry : originals.entrySet()) {
            Object original = entry.getValue();
            if (needsTransactionalProxy(original.getClass())) {
                Object proxy = TransactionalProxyFactory.createProxy(original, entityManagerFactory);
                beans.put(entry.getKey(), proxy);
                log.info("@Transactional proxy: " + original.getClass().getSimpleName());
            }
        }
    }

    private void injectDependencies() throws IllegalAccessException {
        for (Object bean : originals.values()) {
            injectFieldsInto(bean);
        }
    }

    // -----------------------------------------------------------------------
    // Inyección — helper compartido (singletons + prototypes + interceptores)
    // -----------------------------------------------------------------------

    private void injectFieldsInto(Object bean) throws IllegalAccessException {
        for (Field field : getAllFields(bean.getClass())) {

            if (field.isAnnotationPresent(Autowired.class)) {
                Qualifier qualifier     = field.getAnnotation(Qualifier.class);
                String    qualifierName = qualifier != null ? qualifier.value() : null;
                Object    dependency    = resolveDependency(field.getType(), qualifierName);

                if (dependency != null) {
                    field.setAccessible(true);
                    field.set(bean, dependency);
                    log.fine("@Autowired: " + bean.getClass().getSimpleName()
                        + "." + field.getName() + " <- " + dependency.getClass().getSimpleName()
                        + (qualifierName != null ? " (@Qualifier(\"" + qualifierName + "\"))" : ""));
                } else {
                    log.warning("@Autowired no resuelto: " + bean.getClass().getSimpleName()
                        + "." + field.getName() + " (" + field.getType().getName() + ")"
                        + (qualifierName != null ? " @Qualifier(\"" + qualifierName + "\")" : ""));
                }
            }

            if (field.isAnnotationPresent(Value.class)) {
                String expr     = field.getAnnotation(Value.class).value();
                String resolved = resolveExpression(expr);
                if (resolved != null) {
                    field.setAccessible(true);
                    field.set(bean, TypeConverter.convert(resolved, field.getType()));
                    log.fine("@Value: " + bean.getClass().getSimpleName()
                        + "." + field.getName() + " = \"" + resolved + "\"");
                }
            }

            if (sharedEmProxy != null && field.isAnnotationPresent(PersistenceContext.class)) {
                injectEmInto(bean);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Instanciación con soporte de @Autowired de constructor + @Qualifier
    // -----------------------------------------------------------------------

    /**
     * Crea una instancia de {@code clazz}: si declara un constructor @Autowired,
     * resuelve sus parámetros (por tipo, o por nombre de bean si llevan @Qualifier)
     * y lo invoca; si no, usa el constructor sin argumentos (comportamiento previo).
     */
    private Object createInstance(Class<?> clazz) throws Exception {
        Constructor<?> autowiredCtor = findAutowiredConstructor(clazz);
        if (autowiredCtor == null) {
            return clazz.getDeclaredConstructor().newInstance();
        }

        Parameter[] params = autowiredCtor.getParameters();
        Object[]    args   = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Qualifier qualifier     = params[i].getAnnotation(Qualifier.class);
            String    qualifierName = qualifier != null ? qualifier.value() : null;
            Object    dependency    = resolveDependency(params[i].getType(), qualifierName);

            if (dependency == null) {
                throw new RuntimeException("No se pudo resolver el parámetro #" + (i + 1)
                    + " (" + params[i].getType().getName() + ") del constructor @Autowired de "
                    + clazz.getName()
                    + (qualifierName != null ? " — @Qualifier(\"" + qualifierName + "\")" : ""));
            }
            args[i] = dependency;
        }

        autowiredCtor.setAccessible(true);
        Object instance = autowiredCtor.newInstance(args);
        log.fine("@Autowired constructor: " + clazz.getSimpleName() + "(" + params.length + " parámetro(s))");
        return instance;
    }

    /** @throws RuntimeException si la clase declara más de un constructor @Autowired. */
    private static Constructor<?> findAutowiredConstructor(Class<?> clazz) {
        Constructor<?> found = null;
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            if (ctor.isAnnotationPresent(Autowired.class)) {
                if (found != null) {
                    throw new RuntimeException("Múltiples constructores @Autowired en "
                        + clazz.getName() + " — solo se permite uno.");
                }
                found = ctor;
            }
        }
        return found;
    }

    /**
     * Resuelve una dependencia (para campo o parámetro de constructor) por tipo,
     * o por nombre de bean si {@code qualifierName} no es null/vacío. Busca primero
     * entre singletons ya creados; si no la encuentra, intenta crearla de forma
     * perezosa entre los componentes registrados o los prototypes.
     */
    private Object resolveDependency(Class<?> type, String qualifierName) {
        if (qualifierName != null && !qualifierName.isEmpty()) {
            Object namedBean = getBeanEnsureCreated(qualifierName);
            return (namedBean != null && type.isAssignableFrom(namedBean.getClass())) ? namedBean : null;
        }

        for (Object candidate : beans.values()) {
            if (type.isAssignableFrom(candidate.getClass())) return candidate;
        }
        for (String beanName : componentClasses.keySet()) {
            if (type.isAssignableFrom(componentClasses.get(beanName))) {
                return createSingletonIfAbsent(beanName);
            }
        }
        for (Class<?> recipe : prototypeRecipes.values()) {
            if (type.isAssignableFrom(recipe)) return createPrototype(recipe);
        }
        return null;
    }

    /** Como {@link #getBean(String)}, pero fuerza la creación perezosa de componentes pendientes. */
    private Object getBeanEnsureCreated(String name) {
        Object existing = beans.get(name);
        if (existing != null) return existing;
        if (componentClasses.containsKey(name)) return createSingletonIfAbsent(name);
        Class<?> recipe = prototypeRecipes.get(name);
        return (recipe != null) ? createPrototype(recipe) : null;
    }

    private void injectEmInto(Object bean) throws IllegalAccessException {
        for (Field field : getAllFields(bean.getClass())) {
            if (field.isAnnotationPresent(PersistenceContext.class)) {
                field.setAccessible(true);
                field.set(bean, sharedEmProxy);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Resolución de expresiones @Value: "${clave}" o "${clave:default}"
    // -----------------------------------------------------------------------

    private String resolveExpression(String expr) {
        if (expr == null || !expr.startsWith("${") || !expr.endsWith("}")) return expr;

        String inner     = expr.substring(2, expr.length() - 1);
        int    colonIdx  = inner.indexOf(':');
        String key       = (colonIdx >= 0) ? inner.substring(0, colonIdx) : inner;
        String defVal    = (colonIdx >= 0) ? inner.substring(colonIdx + 1) : null;

        String value = properties.get(key);
        if (value != null) return value;
        if (defVal != null) return defVal;

        log.warning("@Value: propiedad no encontrada: '" + key + "' (expresión: " + expr + ")");
        return null;
    }

    // -----------------------------------------------------------------------
    // Factory de prototypes
    // -----------------------------------------------------------------------

    private void createRepositoryProxies() {
        if (pendingRepositoryInterfaces.isEmpty()) return;
        if (entityManagerFactory == null) {
            log.warning("JpaRepository detectados pero no se configuró <jpa>.");
            return;
        }
        for (Class<?> iface : pendingRepositoryInterfaces) {
            Class<?> entityClass = resolveEntityClass(iface);
            if (entityClass == null) continue;
            String beanName = toBeanName(iface.getSimpleName());
            Object proxy = RepositoryProxyFactory.createProxy(iface, entityClass, entityManagerFactory);
            beans.put(beanName, proxy);
            originals.put(beanName, proxy);
            log.info("JpaRepository proxy: " + beanName + "<" + entityClass.getSimpleName() + ">");
        }
    }

    private Object createPrototype(Class<?> clazz) {
        try {
            Object instance = createInstance(clazz);
            injectFieldsInto(instance);
            if (entityManagerFactory != null && needsTransactionalProxy(clazz)) {
                instance = TransactionalProxyFactory.createProxy(instance, entityManagerFactory);
            }
            log.info("@Scope(prototype): " + clazz.getSimpleName()
                + "@" + Integer.toHexString(System.identityHashCode(instance)));
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear prototipo: " + clazz.getName(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers internos
    // -----------------------------------------------------------------------

    private void addSingleton(String name, Object instance) {
        beans.put(name, instance);
        originals.put(name, instance);
    }

    private static boolean isPrototypeScope(Class<?> clazz) {
        Scope scope = clazz.getAnnotation(Scope.class);
        return scope != null && BeanScope.PROTOTYPE.equalsIgnoreCase(scope.value());
    }

    private static boolean isJpaRepositoryInterface(Class<?> clazz) {
        if (!clazz.isInterface()) return false;
        for (Class<?> iface : clazz.getInterfaces()) {
            if (iface == JpaRepository.class) return true;
        }
        return false;
    }

    private static Class<?> resolveEntityClass(Class<?> repositoryInterface) {
        for (Type genericIface : repositoryInterface.getGenericInterfaces()) {
            if (!(genericIface instanceof ParameterizedType)) continue;
            ParameterizedType pt = (ParameterizedType) genericIface;
            if (pt.getRawType() == JpaRepository.class) {
                Type entityType = pt.getActualTypeArguments()[0];
                if (entityType instanceof Class) return (Class<?>) entityType;
            }
        }
        return null;
    }

    private static boolean isComponentAnnotated(Class<?> clazz) {
        for (java.lang.annotation.Annotation ann : clazz.getAnnotations()) {
            Class<? extends java.lang.annotation.Annotation> annType = ann.annotationType();
            if (annType == Component.class) return true;
            if (annType.isAnnotationPresent(Component.class)) return true;
        }
        return false;
    }

    private static boolean needsTransactionalProxy(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Transactional.class)) return true;
        for (Method m : clazz.getMethods()) {
            if (m.isAnnotationPresent(Transactional.class)) return true;
        }
        return false;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static String toBeanName(String simpleName) {
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    // -----------------------------------------------------------------------
    // ApplicationContext API
    // -----------------------------------------------------------------------

    /** Busca primero entre los singletons ya instanciados; si no está, crea una nueva instancia si {@code name} es un prototype registrado. */
    @Override
    public Object getBean(String name) {
        Object singleton = beans.get(name);
        if (singleton != null) return singleton;
        Class<?> recipe = prototypeRecipes.get(name);
        return (recipe != null) ? createPrototype(recipe) : null;
    }

    /** Delega en {@link #getBean(String)} y castea el resultado al tipo indicado. */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> type) {
        return (T) getBean(name);
    }

    /** Busca primero entre los singletons ya instanciados; si no encuentra, busca entre los prototypes registrados y crea una instancia nueva. */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        for (Object bean : beans.values()) {
            if (type.isAssignableFrom(bean.getClass())) return (T) bean;
        }
        for (Class<?> recipe : prototypeRecipes.values()) {
            if (type.isAssignableFrom(recipe)) return (T) createPrototype(recipe);
        }
        return null;
    }

    /** Retorna una vista inmutable de los singletons instanciados (no incluye prototypes). */
    @Override
    public Collection<Object> getAllBeans() {
        return Collections.unmodifiableCollection(beans.values());
    }

    /** Retorna el ViewResolver configurado vía {@code <view-resolver>}, o el default si no se configuró ninguno. */
    @Override
    public ViewResolver getViewResolver() { return viewResolver; }

    /** Retorna el valor de la propiedad cargada desde XML, o null si no existe. */
    @Override
    public String getProperty(String key) { return properties.get(key); }

    /** Retorna el valor de la propiedad, o {@code defaultValue} si no está definida. */
    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /** Retorna una vista inmutable de los interceptores, en el orden en que fueron declarados en {@code <interceptors>}. */
    @Override
    public List<HandlerInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }
}

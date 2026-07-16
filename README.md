
# Nimbus Framework

https://github.com/neossoftware/nimbus-framework

Framework MVC ligero para Java, inspirado en Spring MVC/Spring Framework, pensado
para entornos con dependencias mínimas y compatibilidad con **Java 8** y
contenedores antiguos como **IBM WebSphere Application Server 8.5** y
**Apache Tomcat 7-9**.

Sin Spring, sin arranque de aplicación pesado: un `DispatcherServlet` clásico,
un `ApplicationContext` basado en XML + escaneo de anotaciones, y reflexión pura
para resolver dependencias, bindear requests y despachar a los controllers.

## Requisitos

- Java 8+
- Maven 3.x
- Un contenedor de servlets (Tomcat 7-9, IBM WAS 8.5, o cualquier compatible con Servlet API 3.0+)

## Instalación

```bash
mvn install
```

Esto instala `com.nimbusframework:nimbus-framework:1.0.0` en el repositorio local
de Maven. Agregalo como dependencia en tu WAR:

```xml
<dependency>
    <groupId>com.nimbusframework</groupId>
    <artifactId>nimbus-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

Y registrá el `DispatcherServlet` en tu `web.xml`:

```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>com.nimbusframework.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>configLocation</param-name>
        <param-value>/WEB-INF/framework-config.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <url-pattern>*.do</url-pattern>
</servlet-mapping>
```

## Features

### MVC

- `@Controller` / `@RestController`, con `@RequestMapping` a nivel de clase o método
  (`produces`/`consumes` opcionales para negociar el `Content-Type`)
- `@GetMapping` / `@PostMapping` (atajos) y `@RequestMapping(method = ...)` para PUT/DELETE/PATCH
- `@PathVariable` (una o varias variables por ruta), con matching segmento a segmento:
  soporta tanto `{var}` como variable de por medio en un solo segmento (p. ej. `/usuario/{id}.do`)
- `@RequestParam` (con `required`/`defaultValue`)
- `@ModelAttribute` (binding de formulario, con o sin nombre de atributo explícito)
- `@RequestBody` (deserialización JSON vía Jackson)
- `Model` / `ModelMap`, `ModelAndView`
- `HttpServletRequest` / `HttpServletResponse` inyectables directamente como parámetros,
  en cualquier combinación — incluyendo respuestas de archivo: si el método escribe
  directo al `response` y retorna `null`, el framework no intenta resolver ninguna vista
- Vistas JSP vía `JspViewResolver` (prefijo/sufijo configurable), redirects con el
  prefijo `"redirect:..."`
- REST: `ResponseEntity<T>` (con factories `ok()`, `created()`, `noContent()`, `status()`),
  serialización JSON automática con Jackson

### Inyección de dependencias

- `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController` — todos
  detectados por `<component-scan base-package="...">`
- **Inyección por campo** (`@Autowired` en un field) y **por constructor**
  (`@Autowired` en un constructor, con resolución recursiva de dependencias sin
  importar el orden de escaneo, y detección de dependencias circulares)
- `@Qualifier("nombreDeBean")` para desambiguar cuando hay más de una implementación
  del mismo tipo — funciona tanto en campos como en parámetros de constructor
- `@Scope("prototype")` — una instancia nueva por cada inyección/`getBean()`
- `@Value("${clave}")` / `@Value("${clave:default}")` — propiedades desde `.properties`
- Beans 100% XML:
  ```xml
  <bean id="miBean" class="com.ejemplo.MiClase">
      <property name="nombre" value="dato literal"/>
      <property name="otroBean" ref="idDeOtroBean"/>
  </bean>
  ```
  `<property>` soporta tanto `value="..."` como `<value>...</value>` anidado.
- `<import resource="classpath:ruta/a/otro-config.xml"/>` — carga configuración
  adicional de forma recursiva (con detección de ciclos); cada archivo importado
  puede tener su propio `<component-scan>`, `<bean>`, etc.

### Validación

- Anotaciones de constraint propias: `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email`
- `@Valid` / `@Validated` en parámetros `@ModelAttribute` o `@RequestBody`
- `BindingResult` — si se declara inmediatamente después del parámetro validado,
  recibe los errores en vez de lanzar excepción; si no, un fallo de validación
  lanza `ValidationException` (capturable con `@ExceptionHandler`)
- **Validación custom**: implementá `Validator` (`supports`/`validate`) y conectalo
  vía `@InitBinder` + `WebDataBinder.setValidator(...)` — reemplaza la validación
  por anotaciones para ese controller
- `Errors` — `reject(codigo)` (error global) y `rejectValue(campo, codigo)` (error de campo)
- `MessageSource` / `ResourceBundleMessageSource` — resuelve los códigos de error
  contra un `.properties`, configurable como bean XML:
  ```xml
  <bean id="messageSource" class="com.nimbusframework.validation.ResourceBundleMessageSource">
      <property name="basename"><value>classpath:mensajes/validacion</value></property>
  </bean>
  ```

### Interceptores

- `HandlerInterceptor` (`preHandle` / `postHandle` / `afterCompletion`), registrados
  en orden vía `<interceptors>` en el XML de configuración

### Manejo de errores

- `@ExceptionHandler` local a un controller (prioridad) o global vía `@ControllerAdvice`.
  Orden de resolución para una excepción dada:
  1. Handlers locales del mismo controller que la lanzó
  2. Handlers globales en clases `@ControllerAdvice`

  Dentro de cada grupo se prefiere el tipo de excepción más específico (el más
  cercano en la jerarquía a la clase concreta lanzada). Los métodos handler pueden
  recibir la excepción, `HttpServletRequest` y/o `HttpServletResponse` como parámetros.

### Persistencia (opcional)

- `JpaRepository<T, ID>` — repositorios auto-implementados vía proxy JDK, con
  paginación (`Page`, `Pageable`, `PageRequest`, `Sort`)
- `@Transactional`, `@PersistenceContext`
- Se activa declarando `<jpa persistence-unit="...">` en el XML de configuración;
  si no se declara, ninguna de estas piezas se instancia (una app puede usar
  Nimbus íntegramente en memoria, sin base de datos — ver el proyecto de ejemplo)

  ```xml
  <jpa persistence-unit="miPU">
      <property name="javax.persistence.jdbc.url" value="${db.url}"/>
      <property name="javax.persistence.jdbc.user" value="${db.user}"/>
  </jpa>
  ```

  `persistence-unit` debe coincidir con una unidad definida en
  `META-INF/persistence.xml` (JPA estándar); las `<property>` se resuelven con
  `${clave}` igual que el resto del XML y se pasan a `Persistence.createEntityManagerFactory(...)`.
  Las interfaces que extienden `JpaRepository<T, ID>` detectadas por el
  `component-scan` se instancian automáticamente como proxy JDK.

### JdbcTemplate (opcional)

- `com.nimbusframework.jdbc.JdbcTemplate` (implementa `JdbcOperations`) — acceso directo
  a SQL, inspirado en el `JdbcTemplate` de Spring pero acotado al núcleo: `execute`,
  `update`, `batchUpdate`, `query`/`queryForObject` (con `RowMapper<T>` o un `Class<T>`
  para valores escalares), `queryForList`/`queryForMap`. Sin `CallableStatement`, sin
  `SqlRowSet`, sin streams. Programar contra `JdbcOperations` (en vez de contra
  `JdbcTemplate` directamente) permite swapear la implementación, por ejemplo con un
  stub en tests.
- No participa de las transacciones de `@Transactional`/`@PersistenceContext` (esas solo
  gestionan el `EntityManager` de la capa JPA) — cada operación abre y cierra su propia
  conexión (autocommit). Es una vía de acceso a datos independiente de JPA, pensada para
  cuando esta última es demasiado o no hay proveedor JPA en el contenedor destino.
- Se declara como cualquier bean XML — no requiere ningún cambio en `XmlApplicationContext`.
  El `DataSource` puede ser:

  ```xml
  <!-- Opción dev/standalone: conexión directa por driver -->
  <bean id="dataSource" class="com.nimbusframework.jdbc.datasource.DriverManagerDataSource">
      <property name="driverClassName" value="${db.driver}"/>
      <property name="url" value="${db.url}"/>
      <property name="username" value="${db.user}"/>
      <property name="password" value="${db.password}"/>
  </bean>

  <!-- Opción WAS/Tomcat: DataSource gestionado por el contenedor, expuesto vía JNDI -->
  <bean id="dataSource" class="com.nimbusframework.jdbc.datasource.JndiObjectFactoryBean">
      <property name="jndiName" value="java:comp/env/jdbc/miDataSource"/>
  </bean>

  <bean id="jdbcTemplate" class="com.nimbusframework.jdbc.JdbcTemplate">
      <property name="dataSource" ref="dataSource"/>
  </bean>
  ```

  `jdbcTemplate` queda disponible para `@Autowired` como cualquier otro bean XML.
  `queryForObject`/`queryForMap` lanzan `EmptyResultDataAccessException` (0 filas) o
  `IncorrectResultSizeDataAccessException` (más de 1 fila) cuando se esperaba exactamente una.

### NamedParameterJdbcTemplate (opcional)

- `com.nimbusframework.jdbc.namedparam.NamedParameterJdbcTemplate` (implementa
  `NamedParameterJdbcOperations`) — igual que `JdbcTemplate` pero con parámetros con
  nombre (`:nombre`) en vez de `?` posicional, inspirado en el `NamedParameterJdbcTemplate`
  de Spring. Los valores se pasan como `Map<String, ?>` o `SqlParameterSource`
  (`MapSqlParameterSource` incluida).
- Un valor `Iterable`/array se expande automáticamente a varios `?` — útil para
  `WHERE id IN (:ids)` con `ids` un `List<Integer>`.
- El parseo de nombres saltea comillas y comentarios (`'...'`, `"..."`, `--`, `/* */`),
  así un `:` dentro de un literal o un comentario no se confunde con un parámetro.
  No soporta la sintaxis `:{x}` de Spring ni su escapeo con backslash.
- Mezclar parámetros `:nombre` con `?` tradicionales en el mismo SQL lanza
  `IllegalArgumentException` (no está permitido, igual que en Spring).
- Se declara como bean XML, envolviendo un `JdbcTemplate` existente o un `DataSource` directo:

  ```xml
  <bean id="namedParameterJdbcTemplate"
        class="com.nimbusframework.jdbc.namedparam.NamedParameterJdbcTemplate">
      <property name="jdbcOperations" ref="jdbcTemplate"/>
  </bean>
  ```

  ```java
  Map<String, Object> params = new HashMap<>();
  params.put("nivel", "AVANZADO");
  List<Curso> cursos = namedParameterJdbcTemplate.query(
      "SELECT * FROM cursos WHERE nivel = :nivel", params, cursoRowMapper);
  ```

## Configuración XML mínima

```xml
<mvc-config>
    <properties file="classpath:app.properties"/>
    <view-resolver prefix="/WEB-INF/views/" suffix=".jsp"/>
    <component-scan base-package="com.miapp"/>

    <interceptors>
        <interceptor class="com.miapp.MiInterceptor"/>
    </interceptors>
</mvc-config>
```

## Ejemplo rápido

```java
@Controller
public class SaludoController {

    private final SaludoService saludoService;

    @Autowired
    public SaludoController(SaludoService saludoService) {
        this.saludoService = saludoService;
    }

    @GetMapping("/saludo/{nombre}.do")
    public String saludar(@PathVariable("nombre") String nombre, Model model) {
        model.addAttribute("mensaje", saludoService.saludar(nombre));
        return "saludo";
    }
}
```

Un ejemplo completo y ejecutable (con más de 15 endpoints cubriendo cada feature)
está en el proyecto hermano `nimbus-example`.

## Estructura de paquetes

```
com.nimbusframework
├── annotation    anotaciones (@Controller, @Autowired, @Valid, @Qualifier, ...)
├── bind          binding de tipos y de @ModelAttribute
├── config        carga de .properties
├── context       ApplicationContext (contenedor de beans)
├── handler       dispatch de rutas, resolución de argumentos
├── jpa           soporte transaccional / EntityManager
├── repository    JpaRepository y paginación
├── scan          escaneo de classpath
├── servlet       DispatcherServlet (front controller)
├── validation    Validator, BindingResult, MessageSource
├── view          resolución de vistas JSP
└── web           Model, ModelAndView, interceptores, ResponseEntity
```

## Licencia

GNU General Public License v3.0 (GPL-3.0). Ver [LICENSE](LICENSE).

## Autor

neossoftware

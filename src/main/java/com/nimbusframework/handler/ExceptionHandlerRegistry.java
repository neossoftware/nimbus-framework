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
package com.nimbusframework.handler;

import com.nimbusframework.annotation.ControllerAdvice;
import com.nimbusframework.annotation.ExceptionHandler;
import com.nimbusframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Registra y resuelve métodos @ExceptionHandler.
 *
 * Prioridad de búsqueda para una excepción dada:
 *   1. Handlers locales en el mismo controlador que lanzó la excepción.
 *   2. Handlers globales en clases @ControllerAdvice.
 *
 * Dentro de cada grupo, se prefiere el tipo de excepción más específico
 * (más cercano en la jerarquía al tipo concreto lanzado).
 *
 * Tipos de parámetro soportados en métodos handler:
 *   - Throwable (o subclase) — recibe la excepción capturada
 *   - HttpServletRequest     — recibe el request actual
 *   - HttpServletResponse    — recibe el response actual
 */
public class ExceptionHandlerRegistry {

    private static final Logger log = Logger.getLogger(ExceptionHandlerRegistry.class.getName());

    /** Handlers globales registrados desde @ControllerAdvice. */
    private final List<HandlerEntry> globalHandlers = new ArrayList<>();

    /** Recorre todos los beans del contexto y registra los handlers globales de las clases @ControllerAdvice. */
    public ExceptionHandlerRegistry(ApplicationContext context) {
        for (Object bean : context.getAllBeans()) {
            Class<?> clazz = bean.getClass();
            if (clazz.isAnnotationPresent(ControllerAdvice.class)) {
                registerFrom(bean, clazz);
                log.info("@ControllerAdvice registrado: " + clazz.getSimpleName());
            }
        }
    }

    // -----------------------------------------------------------------------
    // API pública
    // -----------------------------------------------------------------------

    /**
     * Busca un handler para la excepción dada e invoca si lo encuentra.
     *
     * @return el valor de retorno del handler (para que DispatcherServlet lo renderice),
     *         o {@code null} si no hay handler registrado para esta excepción.
     */
    public Object handle(Throwable ex, Object controller,
                         HttpServletRequest req, HttpServletResponse res) throws Exception {

        // 1. Handlers locales (mismo controlador)
        if (controller != null) {
            List<HandlerEntry> local = buildLocalHandlers(controller);
            HandlerEntry entry = findBestHandler(ex, local);
            if (entry != null) {
                log.fine("@ExceptionHandler local: " + entry.method.getName() + " <- " + ex.getClass().getSimpleName());
                return invoke(entry, ex, req, res);
            }
        }

        // 2. Handlers globales (@ControllerAdvice)
        HandlerEntry global = findBestHandler(ex, globalHandlers);
        if (global != null) {
            log.fine("@ExceptionHandler global: " + global.method.getName() + " <- " + ex.getClass().getSimpleName());
            return invoke(global, ex, req, res);
        }

        return null;
    }

    // -----------------------------------------------------------------------
    // Internos
    // -----------------------------------------------------------------------

    private void registerFrom(Object bean, Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(ExceptionHandler.class)) {
                Class<?>[] types = resolveExceptionTypes(m);
                globalHandlers.add(new HandlerEntry(bean, m, types));
                log.info("@ExceptionHandler global: " + clazz.getSimpleName()
                    + "." + m.getName() + " <- " + typesStr(types));
            }
        }
    }

    private List<HandlerEntry> buildLocalHandlers(Object controller) {
        List<HandlerEntry> list = new ArrayList<>();
        for (Method m : controller.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(ExceptionHandler.class)) {
                list.add(new HandlerEntry(controller, m, resolveExceptionTypes(m)));
            }
        }
        return list;
    }

    /**
     * Elige el handler cuyo tipo de excepción declarado es el más específico
     * para el tipo concreto lanzado (camina la jerarquía de más a menos específico).
     */
    private HandlerEntry findBestHandler(Throwable ex, List<HandlerEntry> handlers) {
        if (handlers.isEmpty()) return null;
        Class<?> exType = ex.getClass();

        // Recorremos la jerarquía de excepción de más a menos específico
        while (exType != null) {
            for (HandlerEntry entry : handlers) {
                for (Class<?> handled : entry.exceptionTypes) {
                    if (handled == exType) return entry;         // match exacto
                }
            }
            // Ningún match exacto en este nivel: subir un nivel
            if (exType == Throwable.class) break;
            exType = exType.getSuperclass();
        }

        // Fallback: cualquier handler cuyo tipo sea asignable (p.ej. Exception.class)
        for (HandlerEntry entry : handlers) {
            for (Class<?> handled : entry.exceptionTypes) {
                if (handled.isAssignableFrom(ex.getClass())) return entry;
            }
        }
        return null;
    }

    private Object invoke(HandlerEntry entry, Throwable ex,
                          HttpServletRequest req, HttpServletResponse res) throws Exception {
        Method     m      = entry.method;
        Parameter[] params = m.getParameters();
        Object[]   args   = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();
            if (Throwable.class.isAssignableFrom(type)) {
                args[i] = ex;
            } else if (HttpServletRequest.class.isAssignableFrom(type)) {
                args[i] = req;
            } else if (HttpServletResponse.class.isAssignableFrom(type)) {
                args[i] = res;
            }
        }

        try {
            return m.invoke(entry.bean, args);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw new RuntimeException(cause);
        }
    }

    /** Determina los tipos de excepción que maneja el método. */
    @SuppressWarnings("unchecked")
    private static Class<?>[] resolveExceptionTypes(Method m) {
        ExceptionHandler ann = m.getAnnotation(ExceptionHandler.class);

        // 1. Tipos explícitos en la anotación
        if (ann.value().length > 0) {
            return ann.value();
        }

        // 2. Inferir del primer parámetro de tipo Throwable
        for (Parameter p : m.getParameters()) {
            if (Throwable.class.isAssignableFrom(p.getType())) {
                return new Class<?>[]{ p.getType() };
            }
        }

        // 3. Fallback: captura cualquier Exception
        return new Class<?>[]{ Exception.class };
    }

    private static String typesStr(Class<?>[] types) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> t : types) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(t.getSimpleName());
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------

    private static final class HandlerEntry {
        final Object    bean;
        final Method    method;
        final Class<?>[] exceptionTypes;

        HandlerEntry(Object bean, Method method, Class<?>[] exceptionTypes) {
            this.bean           = bean;
            this.method         = method;
            this.exceptionTypes = exceptionTypes;
        }
    }
}

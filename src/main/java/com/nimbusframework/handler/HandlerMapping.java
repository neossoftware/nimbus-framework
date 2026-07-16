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

import com.nimbusframework.annotation.Controller;
import com.nimbusframework.annotation.GetMapping;
import com.nimbusframework.annotation.PostMapping;
import com.nimbusframework.annotation.RequestMapping;
import com.nimbusframework.annotation.RestController;
import com.nimbusframework.context.ApplicationContext;
import com.nimbusframework.validation.MessageSource;
import com.nimbusframework.web.RequestMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Construye y resuelve los mappings URL → HandlerMethod.
 *
 * Soporta:
 *   - @GetMapping / @PostMapping (legado)
 *   - @RequestMapping a nivel de clase (prefijo de ruta)
 *   - @RequestMapping a nivel de método (ruta + verbo HTTP)
 *   - @RestController (responde JSON)
 *   - Verbos: GET, POST, PUT, DELETE, PATCH
 *   - Variables de path: /cursos/{id}
 */
public class HandlerMapping {

    private static final Logger log = Logger.getLogger(HandlerMapping.class.getName());

    // verb → exact path → HandlerMethod
    private final Map<String, Map<String, HandlerMethod>> exactHandlers  = new HashMap<>();
    // verb → list of PathPattern
    private final Map<String, List<PathPattern>>          patternHandlers = new HashMap<>();

    private static final List<String> ALL_VERBS =
        Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH");

    public HandlerMapping(ApplicationContext context) {
        for (String verb : ALL_VERBS) {
            exactHandlers.put(verb, new HashMap<>());
            patternHandlers.put(verb, new ArrayList<>());
        }

        // Bean "messageSource" opcional (ResourceBundleMessageSource vía XML) — si no
        // existe, los HandlerMethod validan igual, pero usando el código/mensaje literal.
        MessageSource messageSource = context.getBean(MessageSource.class);

        for (Object bean : context.getAllBeans()) {
            Class<?> clazz = bean.getClass();
            boolean isController     = clazz.isAnnotationPresent(Controller.class);
            boolean isRestController = clazz.isAnnotationPresent(RestController.class);
            if (isController || isRestController) {
                registerController(bean, isRestController, messageSource);
            }
        }
    }

    private void registerController(Object controller, boolean isRest, MessageSource messageSource) {
        Class<?> clazz = controller.getClass();

        // Prefijo de clase via @RequestMapping("/api/cursos")
        String classPrefix = "";
        if (clazz.isAnnotationPresent(RequestMapping.class)) {
            classPrefix = clazz.getAnnotation(RequestMapping.class).value();
        }

        for (Method method : clazz.getDeclaredMethods()) {

            // @GetMapping (legado + prefijo de clase)
            if (method.isAnnotationPresent(GetMapping.class)) {
                String path = classPrefix + method.getAnnotation(GetMapping.class).value();
                register("GET", path, new HandlerMethod(controller, method, isRest, messageSource));
            }

            // @PostMapping (legado + prefijo de clase)
            if (method.isAnnotationPresent(PostMapping.class)) {
                String path = classPrefix + method.getAnnotation(PostMapping.class).value();
                register("POST", path, new HandlerMethod(controller, method, isRest, messageSource));
            }

            // @RequestMapping a nivel de método
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping rm = method.getAnnotation(RequestMapping.class);
                String path       = classPrefix + rm.value();
                RequestMethod[] verbs = rm.method();

                if (verbs.length == 0) {
                    // Sin restricción de verbo → registrar GET y POST
                    register("GET",  path, new HandlerMethod(controller, method, isRest, messageSource));
                    register("POST", path, new HandlerMethod(controller, method, isRest, messageSource));
                } else {
                    for (RequestMethod verb : verbs) {
                        register(verb.name(), path, new HandlerMethod(controller, method, isRest, messageSource));
                    }
                }
            }
        }
    }

    private void register(String verb, String path, HandlerMethod hm) {
        String controllerName = hm.getController().getClass().getSimpleName();
        String methodName     = hm.getMethod().getName();
        log.info(String.format("%-7s %s -> %s.%s()", verb, path, controllerName, methodName));

        Map<String, HandlerMethod> exact    = exactHandlers.get(verb);
        List<PathPattern>          patterns = patternHandlers.get(verb);

        if (exact == null) {
            log.warning("Verbo HTTP no soportado: " + verb);
            return;
        }

        if (path.contains("{")) {
            patterns.add(new PathPattern(path, hm));
        } else {
            exact.put(path, hm);
        }
    }

    /**
     * Resuelve el handler para la ruta y verbo dados.
     * 1. Match exacto (O(1))
     * 2. Match por patrón con variables (O(n))
     */
    public HandlerExecution getHandler(String path, String httpMethod) {
        String verb = httpMethod.toUpperCase();

        Map<String, HandlerMethod> exact    = exactHandlers.get(verb);
        List<PathPattern>          patterns = patternHandlers.get(verb);

        if (exact == null) return null;

        // 1. Match exacto
        HandlerMethod hm = exact.get(path);
        if (hm != null) {
            return new HandlerExecution(hm, Collections.emptyMap());
        }

        // 2. Match por patrón
        for (PathPattern pattern : patterns) {
            Map<String, String> vars = pattern.match(path);
            if (vars != null) {
                log.fine("PathPattern: " + pattern.getTemplate() + " vars=" + vars);
                return new HandlerExecution(pattern.getHandlerMethod(), vars);
            }
        }

        return null;
    }
}

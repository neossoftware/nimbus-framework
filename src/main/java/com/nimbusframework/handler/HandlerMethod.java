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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusframework.annotation.InitBinder;
import com.nimbusframework.annotation.ModelAttribute;
import com.nimbusframework.annotation.PathVariable;
import com.nimbusframework.annotation.RequestBody;
import com.nimbusframework.annotation.RequestParam;
import com.nimbusframework.annotation.Valid;
import com.nimbusframework.annotation.Validated;
import com.nimbusframework.validation.BeanValidator;
import com.nimbusframework.validation.BindingResult;
import com.nimbusframework.validation.ConstraintViolation;
import com.nimbusframework.validation.MessageSource;
import com.nimbusframework.validation.ValidationException;
import com.nimbusframework.validation.ValidationResult;
import com.nimbusframework.validation.Validator;
import com.nimbusframework.bind.ModelAttributeBinder;
import com.nimbusframework.bind.TypeConverter;
import com.nimbusframework.web.Model;
import com.nimbusframework.web.ModelAndView;
import com.nimbusframework.web.ModelMap;
import com.nimbusframework.web.WebDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Representa el par controlador + método que atiende una URL.
 *
 * Parámetros soportados (en cualquier orden, salvo BindingResult):
 *   HttpServletRequest, HttpServletResponse, Model/ModelMap, {@code @PathVariable}, {@code @RequestParam},
 *   {@code @ModelAttribute}, {@code @RequestBody} (deserializado desde JSON)
 *
 * {@code @ModelAttribute("nombre")} expone el bean bindeado en el Model bajo ese nombre
 * (o el nombre de la clase decapitalizado si no se indica). Si el parámetro
 * también tiene {@code @Valid}/{@code @Validated}, se valida así:
 *   1. Se invocan los métodos {@code @InitBinder} del controller con un WebDataBinder nuevo.
 *   2. Si alguno conectó un Validator (binder.setValidator(...)) que soporta el
 *      tipo del bean, se usa ese Validator (validate(bean, errors)).
 *   3. Si no, se usan las anotaciones {@code @NotBlank}/{@code @Size}/etc. vía BeanValidator.
 * El resultado se entrega al parámetro BindingResult que siga inmediatamente en
 * la firma, o si no hay uno, un fallo de validación lanza ValidationException.
 *
 * Tipos de retorno:
 *   - {@code @Controller}  → String (view name o "redirect:..."), ModelAndView
 *   - {@code @RestController} → cualquier Object serializable a JSON, {@code ResponseEntity<T>}
 */
public class HandlerMethod {

    private static final Logger       log    = Logger.getLogger(HandlerMethod.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Object        controller;
    private final Method        method;
    private final boolean       restController;
    private final MessageSource messageSource; // nullable — bean "messageSource" del ApplicationContext, si existe

    /** Igual que {@link #HandlerMethod(Object, Method, boolean, MessageSource)} pero sin MessageSource (sin i18n de validación). */
    public HandlerMethod(Object controller, Method method, boolean restController) {
        this(controller, method, restController, null);
    }

    /**
     * @param controller bean controlador dueño del método.
     * @param method método handler a invocar.
     * @param restController true si el controlador es @RestController (responde JSON en vez de view name).
     * @param messageSource bean opcional para resolver mensajes de validación i18n; puede ser null.
     */
    public HandlerMethod(Object controller, Method method, boolean restController, MessageSource messageSource) {
        this.controller     = controller;
        this.method         = method;
        this.restController = restController;
        this.messageSource  = messageSource;
    }

    /** true si el controlador es @RestController (responde JSON en vez de view name). */
    public boolean isRestController() { return restController; }
    /** Retorna el bean controlador dueño de este handler. */
    public Object  getController()    { return controller; }
    /** Retorna el método reflejado que se invoca. */
    public Method  getMethod()        { return method; }

    /**
     * Invoca el método y retorna:
     *   - String (view name o redirect) para @Controller
     *   - cualquier Object para @RestController
     */
    public Object invoke(HttpServletRequest request, HttpServletResponse response,
                         Map<String, String> pathVariables) throws Exception {

        String label = controller.getClass().getSimpleName() + "." + method.getName() + "()";
        log.fine("Invocando " + label);

        Model    model  = new ModelMap();
        Object[] args   = resolveArguments(request, response, model, pathVariables);
        Object   result = method.invoke(controller, args);

        if (!restController) {
            // MVC: aplica modelo al request y devuelve view name
            if (result instanceof ModelAndView) {
                ModelAndView mav = (ModelAndView) result;
                mav.getModel().applyToRequest(request);
                model.applyToRequest(request);
                return mav.getViewName();
            }
            model.applyToRequest(request);
            return (String) result;
        }

        // REST: devuelve el objeto tal cual (DispatcherServlet serializa a JSON)
        return result;
    }

    // ------------------------------------------------------------------

    private Object[] resolveArguments(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Model model,
                                       Map<String, String> pathVariables) throws Exception {
        Parameter[] params = method.getParameters();
        Object[]    args   = new Object[params.length];

        // BindingResult del último @ModelAttribute/@RequestBody bindeado, pendiente
        // de ser consumido por un parámetro BindingResult inmediatamente siguiente.
        BindingResult pendingBindingResult = null;

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            Class<?>  type  = param.getType();

            if (BindingResult.class.isAssignableFrom(type)) {
                if (pendingBindingResult == null) {
                    throw new IllegalStateException(
                        "BindingResult debe declararse inmediatamente después del parámetro "
                        + "@ModelAttribute/@RequestBody que valida, en "
                        + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()");
                }
                args[i] = pendingBindingResult;
                pendingBindingResult = null;
                continue;
            }

            // Cualquier otro parámetro "consume" el BindingResult pendiente: si no fue
            // seguido de un BindingResult y tiene errores, falla ahora.
            if (pendingBindingResult != null && pendingBindingResult.hasErrors()) {
                throw new ValidationException(pendingBindingResult);
            }
            pendingBindingResult = null;

            if (HttpServletRequest.class.isAssignableFrom(type)) {
                args[i] = request;

            } else if (HttpServletResponse.class.isAssignableFrom(type)) {
                args[i] = response;

            } else if (Model.class.isAssignableFrom(type)) {
                args[i] = model;

            } else if (param.isAnnotationPresent(PathVariable.class)) {
                args[i] = resolvePathVariable(param, pathVariables);

            } else if (param.isAnnotationPresent(RequestBody.class)) {
                String body;
                try (BufferedReader reader = request.getReader()) {
                    body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
                RequestBody rb = param.getAnnotation(RequestBody.class);
                if (body == null || body.isEmpty()) {
                    if (rb.required()) {
                        throw new IllegalArgumentException("@RequestBody requerido pero cuerpo vacío");
                    }
                    args[i] = null;
                } else {
                    Object bodyObj = MAPPER.readValue(body, type);
                    args[i] = bodyObj;
                    if (param.isAnnotationPresent(Valid.class) || param.isAnnotationPresent(Validated.class)) {
                        pendingBindingResult = validate(bodyObj, decapitalize(type.getSimpleName()));
                    }
                }

            } else if (param.isAnnotationPresent(ModelAttribute.class)) {
                ModelAttribute ma       = param.getAnnotation(ModelAttribute.class);
                Object         bound    = ModelAttributeBinder.bind(type, request);
                String         attrName = !ma.value().isEmpty() ? ma.value() : decapitalize(type.getSimpleName());

                if (model != null) {
                    model.addAttribute(attrName, bound);
                }
                args[i] = bound;

                if (param.isAnnotationPresent(Valid.class) || param.isAnnotationPresent(Validated.class)) {
                    pendingBindingResult = validate(bound, attrName);
                }

            } else if (param.isAnnotationPresent(RequestParam.class)) {
                args[i] = resolveRequestParam(param, request);
            }
        }

        if (pendingBindingResult != null && pendingBindingResult.hasErrors()) {
            throw new ValidationException(pendingBindingResult);
        }

        return args;
    }

    /**
     * Valida {@code target}: primero invoca los métodos @InitBinder del controller
     * para ver si conectan un Validator custom; si no, cae a BeanValidator (anotaciones).
     */
    private BindingResult validate(Object target, String objectName) throws Exception {
        WebDataBinder binder = new WebDataBinder();
        invokeInitBinders(binder);

        BindingResult bindingResult = new BindingResult(objectName, messageSource);
        Validator     customValidator = binder.getValidator();

        if (target != null && customValidator != null && customValidator.supports(target.getClass())) {
            customValidator.validate(target, bindingResult);
        } else {
            ValidationResult vr = BeanValidator.validate(target);
            for (ConstraintViolation v : vr.getViolations()) {
                bindingResult.rejectValue(v.getField(), null, v.getMessage());
            }
        }
        return bindingResult;
    }

    private void invokeInitBinders(WebDataBinder binder) throws Exception {
        for (Method m : controller.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(InitBinder.class)) {
                m.setAccessible(true);
                m.invoke(controller, binder);
            }
        }
    }

    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private Object resolvePathVariable(Parameter param, Map<String, String> pathVariables) {
        PathVariable pv  = param.getAnnotation(PathVariable.class);
        String rawValue  = pathVariables.get(pv.value());
        if (rawValue == null) {
            throw new IllegalArgumentException("@PathVariable '" + pv.value() + "' no encontrada");
        }
        return TypeConverter.convert(rawValue, param.getType());
    }

    private Object resolveRequestParam(Parameter param, HttpServletRequest request) {
        RequestParam rp       = param.getAnnotation(RequestParam.class);
        String       rawValue = request.getParameter(rp.value());

        if (rawValue == null || rawValue.isEmpty()) {
            if (!rp.defaultValue().isEmpty()) {
                rawValue = rp.defaultValue();
            } else if (rp.required()) {
                throw new IllegalArgumentException(
                    "@RequestParam requerido no recibido: '" + rp.value() + "'");
            } else {
                return null;
            }
        }
        return TypeConverter.convert(rawValue, param.getType());
    }
}

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
package com.nimbusframework.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusframework.context.ApplicationContext;
import com.nimbusframework.context.XmlApplicationContext;
import com.nimbusframework.handler.ExceptionHandlerRegistry;
import com.nimbusframework.handler.HandlerExecution;
import com.nimbusframework.handler.HandlerMapping;
import com.nimbusframework.view.ViewResolver;
import com.nimbusframework.web.HandlerInterceptor;
import com.nimbusframework.web.InterceptorChain;
import com.nimbusframework.web.ResponseEntity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Front Controller del framework. Recibe todas las peticiones y las despacha.
 *
 * Soporta:
 *   - MVC  (@Controller)     → forward a vista JSP o redirect
 *   - REST (@RestController) → serialización JSON directa
 *   - Manejo de errores via @ExceptionHandler / @ControllerAdvice
 *
 * URL patterns recomendados en web.xml:
 *   *.do     → controladores MVC
 *   /api/*   → controladores REST
 */
public class DispatcherServlet extends HttpServlet {

    private static final Logger       log    = Logger.getLogger(DispatcherServlet.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HandlerMapping             handlerMapping;
    private ExceptionHandlerRegistry   exceptionRegistry;
    private ViewResolver               viewResolver;
    private List<HandlerInterceptor>   interceptors;

    /**
     * Lee el init-param "configLocation" (default "/WEB-INF/framework-config.xml"),
     * construye el ApplicationContext desde ese XML y arma handlerMapping,
     * exceptionRegistry, viewResolver e interceptors.
     *
     * @throws ServletException si no encuentra el archivo de configuración.
     */
    @Override
    public void init() throws ServletException {
        String configLocation = getInitParameter("configLocation");
        if (configLocation == null) configLocation = "/WEB-INF/framework-config.xml";

        log.info("Inicializando MyWebFramework. Config: " + configLocation);

        InputStream configStream = getServletContext().getResourceAsStream(configLocation);
        if (configStream == null) {
            throw new ServletException("Configuración del framework no encontrada: " + configLocation);
        }

        ApplicationContext context = new XmlApplicationContext(configStream);
        handlerMapping    = new HandlerMapping(context);
        exceptionRegistry = new ExceptionHandlerRegistry(context);
        viewResolver      = context.getViewResolver();
        interceptors      = context.getInterceptors();

        log.info("MyWebFramework listo.");
    }

    // -----------------------------------------------------------------------
    // Verbos HTTP
    // -----------------------------------------------------------------------

    /** Despacha GET a través del front controller. */
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { handle(req, res, "GET"); }

    /** Despacha POST a través del front controller. */
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { handle(req, res, "POST"); }

    /** Despacha PUT a través del front controller. */
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { handle(req, res, "PUT"); }

    /** Despacha DELETE a través del front controller. */
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException { handle(req, res, "DELETE"); }

    /** PATCH no está en HttpServlet — lo interceptamos en service(). */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            handle(req, res, "PATCH");
        } else {
            super.service(req, res);
        }
    }

    // -----------------------------------------------------------------------
    // Dispatch principal
    // -----------------------------------------------------------------------

    private void handle(HttpServletRequest request, HttpServletResponse response, String method)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        String pathInfo    = request.getPathInfo();
        String path        = (pathInfo != null) ? servletPath + pathInfo : servletPath;

        log.fine("[" + method + "] " + path);

        HandlerExecution execution = handlerMapping.getHandler(path, method);

        if (execution == null) {
            log.warning("Sin handler para: [" + method + "] " + path);
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Sin handler para: [" + method + "] " + path);
            return;
        }

        InterceptorChain chain = new InterceptorChain(interceptors);

        // preHandleOk: si es false, InterceptorChain ya llamó afterCompletion internamente
        boolean  preHandleOk = false;
        Exception thrownEx   = null;
        try {
            preHandleOk = chain.applyPreHandle(request, response, execution);
            if (!preHandleOk) return;

            Object result = execution.invoke(request, response);
            chain.applyPostHandle(request, response, execution);

            if (!response.isCommitted()) {
                render(result, request, response, execution.isRestController());
            }

        } catch (Exception rawEx) {
            thrownEx = rawEx;
            if (!response.isCommitted()) {
                Throwable actual = unwrap(rawEx);
                log.log(Level.WARNING,
                    "Excepción en [" + method + "] " + path + ": "
                    + actual.getClass().getSimpleName() + " — " + actual.getMessage());

                Object errorResult = null;
                try {
                    errorResult = exceptionRegistry.handle(
                        actual, execution.getController(), request, response);
                } catch (Exception handlerEx) {
                    log.log(Level.SEVERE, "Error en @ExceptionHandler", handlerEx);
                }

                if (!response.isCommitted()) {
                    if (errorResult != null) {
                        render(errorResult, request, response, true);
                    } else {
                        log.log(Level.SEVERE, "Excepción no manejada: [" + method + "] " + path, actual);
                        writeJsonError(actual, response);
                    }
                }
            }
        } finally {
            // afterCompletion solo si preHandle completó — si falló, ya lo llamó applyPreHandle
            if (preHandleOk) {
                chain.triggerAfterCompletion(request, response, execution, thrownEx);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------

    /**
     * Decide cómo renderizar el resultado del handler:
     *   - null o response committed → nada
     *   - String en un @Controller  → view name (redirect o forward)
     *   - ResponseEntity / cualquier otro Object → JSON
     */
    private void render(Object result, HttpServletRequest request,
                        HttpServletResponse response, boolean isRest)
            throws ServletException, IOException {

        if (result == null || response.isCommitted()) return;

        if (!isRest && result instanceof String) {
            handleViewResult((String) result, request, response);
        } else {
            handleJsonResult(result, response);
        }
    }

    private void handleViewResult(String viewName, HttpServletRequest request,
                                   HttpServletResponse response)
            throws ServletException, IOException {
        if (viewName.startsWith("redirect:")) {
            String url = viewName.substring("redirect:".length());
            log.fine("Redirect -> " + url);
            response.sendRedirect(request.getContextPath() + url);
        } else {
            String viewPath = viewResolver.resolve(viewName);
            log.fine("Forward -> " + viewPath);
            request.getRequestDispatcher(viewPath).forward(request, response);
        }
    }

    private void handleJsonResult(Object result, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");

        if (result instanceof ResponseEntity) {
            ResponseEntity<?> re = (ResponseEntity<?>) result;

            re.getHeaders().forEach((name, values) ->
                values.forEach(v -> response.addHeader(name, v)));

            response.setStatus(re.getStatus().value());

            Object body = re.getBody();
            if (body != null) {
                if (!response.containsHeader("Content-Type")) {
                    response.setContentType("application/json;charset=UTF-8");
                }
                MAPPER.writeValue(response.getWriter(), body);
            }
        } else {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            MAPPER.writeValue(response.getWriter(), result);
        }
    }

    private void writeJsonError(Throwable e, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");
        String msg = (e.getMessage() != null ? e.getMessage() : "Internal Server Error")
                         .replace("\"", "'");
        response.getWriter().write("{\"error\":\"" + msg + "\",\"type\":\""
            + e.getClass().getSimpleName() + "\"}");
    }

    // -----------------------------------------------------------------------

    /** Extrae la causa real de un InvocationTargetException. */
    private static Throwable unwrap(Throwable t) {
        if (t instanceof InvocationTargetException && t.getCause() != null) {
            return t.getCause();
        }
        return t;
    }
}

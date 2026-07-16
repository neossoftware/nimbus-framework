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
package com.nimbusframework.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ejecuta la cadena de {@link HandlerInterceptor} para una petición HTTP.
 *
 * Se crea una instancia por petición para mantener el estado (interceptorIndex)
 * thread-safe sin necesidad de sincronización.
 *
 * Ciclo:
 *   1. applyPreHandle()       — llama preHandle() en orden; si alguno retorna false, aborta
 *   2. [handler ejecuta]
 *   3. applyPostHandle()      — llama postHandle() en orden
 *   4. triggerAfterCompletion — llama afterCompletion() en orden INVERSO (siempre)
 */
public class InterceptorChain {

    private static final Logger log = Logger.getLogger(InterceptorChain.class.getName());

    private final List<HandlerInterceptor> interceptors;
    /** Índice del último interceptor que ejecutó preHandle con éxito. */
    private int interceptorIndex = -1;

    public InterceptorChain(List<HandlerInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * Ejecuta preHandle() en cada interceptor.
     * Si alguno retorna false, llama triggerAfterCompletion() y retorna false.
     */
    public boolean applyPreHandle(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Object handler) throws Exception {
        for (int i = 0; i < interceptors.size(); i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            if (!interceptor.preHandle(request, response, handler)) {
                // El interceptor ya escribió la respuesta (ej. 401); llamar afterCompletion
                triggerAfterCompletion(request, response, handler, null);
                return false;
            }
            interceptorIndex = i;
        }
        return true;
    }

    /**
     * Ejecuta postHandle() en orden directo.
     * Solo se llama si applyPreHandle() retornó true y el handler ejecutó sin excepción.
     */
    public void applyPostHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            interceptor.postHandle(request, response, handler);
        }
    }

    /**
     * Ejecuta afterCompletion() en orden INVERSO para los interceptores que llegaron a preHandle.
     * Siempre se llama, tanto en éxito como en error.
     *
     * @param ex la excepción (antes de ser manejada), o null si no hubo error.
     */
    public void triggerAfterCompletion(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {
        for (int i = interceptorIndex; i >= 0; i--) {
            try {
                interceptors.get(i).afterCompletion(request, response, handler, ex);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error en afterCompletion del interceptor #" + i, e);
            }
        }
    }
}

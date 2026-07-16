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

/**
 * Intercepta peticiones HTTP antes y después de que el handler (controlador) las procese.
 *
 * Ciclo de vida por petición:
 *   1. preHandle()       — antes del handler; retorna false para abortar
 *   2. [handler ejecuta]
 *   3. postHandle()      — después del handler, antes de renderizar la respuesta
 *   4. afterCompletion() — siempre, incluso si hubo excepción
 *
 * Casos de uso típicos: autenticación, auditoría, logging de tiempos, CORS, rate limiting.
 *
 * Registro en framework-config.xml:
 * <pre>
 *   {@code
 *   <interceptors>
 *     <interceptor class="com.example.interceptor.AuthInterceptor"/>
 *   </interceptors>
 *   }
 * </pre>
 */
public interface HandlerInterceptor {

    /**
     * Se ejecuta ANTES del handler.
     * @return true para continuar con la cadena; false para abortar (la respuesta ya debe estar escrita).
     */
    default boolean preHandle(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler) throws Exception {
        return true;
    }

    /**
     * Se ejecuta DESPUÉS del handler, antes de renderizar la vista o escribir JSON.
     * No se llama si preHandle() retornó false.
     */
    default void postHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {}

    /**
     * Se ejecuta SIEMPRE al final, incluso si hubo excepción.
     * Se llama en orden inverso al de preHandle.
     * @param ex la excepción lanzada, o null si todo fue bien.
     */
    default void afterCompletion(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Object handler,
                                  Exception ex) throws Exception {}
}

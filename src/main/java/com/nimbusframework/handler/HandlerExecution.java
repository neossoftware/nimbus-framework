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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Resultado de un match en el HandlerMapping.
 * Lleva el HandlerMethod a invocar y las variables de path ya extraídas.
 */
public class HandlerExecution {

    private final HandlerMethod       handlerMethod;
    private final Map<String, String> pathVariables;

    /**
     * @param handlerMethod el handler a invocar.
     * @param pathVariables las variables de path ya extraídas del match en HandlerMapping.
     */
    public HandlerExecution(HandlerMethod handlerMethod, Map<String, String> pathVariables) {
        this.handlerMethod = handlerMethod;
        this.pathVariables = pathVariables;
    }

    /** true si el controlador es @RestController (respuesta JSON). */
    public boolean isRestController() {
        return handlerMethod.isRestController();
    }

    /** Retorna el bean controlador — usado por ExceptionHandlerRegistry para handlers locales. */
    public Object getController() {
        return handlerMethod.getController();
    }

    /**
     * Invoca el handler y retorna:
     *   - String para @Controller (view name o "redirect:...")
     *   - Object para @RestController (serializado a JSON por DispatcherServlet)
     */
    public Object invoke(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return handlerMethod.invoke(request, response, pathVariables);
    }
}

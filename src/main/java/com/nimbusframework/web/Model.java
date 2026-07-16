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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contenedor de atributos que el controlador quiere pasar a la vista.
 * El DispatcherServlet llama a applyToRequest() antes del forward al JSP,
 * volcando todo como request.setAttribute().
 *
 * Uso en el controlador:
 * <pre>
 *   public String home(HttpServletRequest req, HttpServletResponse res, Model model) {
 *       model.addAttribute("usuario", new Usuario("Ana"));
 *       return "home";
 *   }
 * </pre>
 */
public class Model {

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    /** Agrega un atributo con el nombre dado. */
    public Model addAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    /** Agrega un atributo usando el nombre simple de la clase de {@code value} como clave. */
    public Model addAttribute(Object value) {
        attributes.put(value.getClass().getSimpleName(), value);
        return this;
    }

    /** @return el valor del atributo, o null si no existe. */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /** @return una vista de solo lectura de todos los atributos. */
    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(attributes);
    }

    /** Vuelca todos los atributos al request para que los JSPs puedan leerlos con EL. */
    public void applyToRequest(HttpServletRequest request) {
        attributes.forEach(request::setAttribute);
    }
}

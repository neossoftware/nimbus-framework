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

import java.util.*;

/**
 * Mapa de cabeceras HTTP con API similar a Spring's HttpHeaders.
 * Las claves son case-insensitive por convención pero se almacenan tal cual.
 */
public class HttpHeaders extends LinkedHashMap<String, List<String>> {

    public static final String CONTENT_TYPE   = "Content-Type";
    public static final String ACCEPT         = "Accept";
    public static final String LOCATION       = "Location";
    public static final String AUTHORIZATION  = "Authorization";

    /** Establece (reemplaza) una cabecera con un único valor. */
    public void set(String name, String value) {
        put(name, Collections.singletonList(value));
    }

    /** Agrega un valor a una cabecera (sin reemplazar valores previos). */
    public void add(String name, String value) {
        computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    /** Obtiene el primer valor de la cabecera, o null si no existe. */
    public String getFirst(String name) {
        List<String> values = get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    /** Establece la cabecera Content-Type. */
    public void setContentType(String contentType) {
        set(CONTENT_TYPE, contentType);
    }

    /** Establece la cabecera Location, típicamente usada en respuestas de redirección. */
    public void setLocation(String location) {
        set(LOCATION, location);
    }
}

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
package com.nimbusframework.view;

/**
 * Resuelve vistas JSP combinando prefijo + nombre + sufijo.
 * Ejemplo: {@code "home"} se resuelve a {@code "/WEB-INF/views/home.jsp"}.
 */
public class JspViewResolver implements ViewResolver {

    private final String prefix;
    private final String suffix;

    /**
     * @param prefix ruta antepuesta al nombre de vista, ej. "/WEB-INF/views/".
     * @param suffix extensión agregada al final, ej. ".jsp".
     */
    public JspViewResolver(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /** Retorna {@code prefix + viewName + suffix}. */
    @Override
    public String resolve(String viewName) {
        return prefix + viewName + suffix;
    }
}

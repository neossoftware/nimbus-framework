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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa un template de URL con variables, por ejemplo "/usuario/{id}.do"
 * o "/curso/{cursoId}/alumno/{alumnoId}.do".
 *
 * Algoritmo de matching (segmento a segmento, separados por "/"):
 *   - Segmento puro {varName}      →  captura todo el segmento
 *   - Segmento embebido prefix{v}suffix  →  captura la parte entre prefix y suffix
 *   - Segmento literal             →  comparación exacta
 *
 * Retorna null si el path no coincide con el template.
 */
public class PathPattern {

    private final String        template;
    private final HandlerMethod handlerMethod;
    private final String[]      templateParts;

    public PathPattern(String template, HandlerMethod handlerMethod) {
        this.template      = template;
        this.handlerMethod = handlerMethod;
        this.templateParts = template.split("/", -1);
    }

    /**
     * Intenta hacer match entre este template y el path concreto.
     * @return mapa de variables extraídas, o null si no hay match.
     */
    public Map<String, String> match(String path) {
        String[] pathParts = path.split("/", -1);
        if (pathParts.length != templateParts.length) return null;

        Map<String, String> vars = new LinkedHashMap<>();
        for (int i = 0; i < templateParts.length; i++) {
            if (!matchSegment(templateParts[i], pathParts[i], vars)) {
                return null;
            }
        }
        return vars;
    }

    private boolean matchSegment(String tmpl, String actual, Map<String, String> vars) {
        // Caso 1: segmento puro "{varName}"
        if (tmpl.startsWith("{") && tmpl.endsWith("}") && tmpl.indexOf('{') == 0
                && tmpl.lastIndexOf('}') == tmpl.length() - 1) {
            vars.put(tmpl.substring(1, tmpl.length() - 1), actual);
            return true;
        }

        // Caso 2: variable embebida dentro de segmento, p.ej. "{id}.do" o "pre{id}suf"
        int varStart = tmpl.indexOf('{');
        int varEnd   = tmpl.indexOf('}');
        if (varStart >= 0 && varEnd > varStart) {
            String prefix  = tmpl.substring(0, varStart);
            String varName = tmpl.substring(varStart + 1, varEnd);
            String suffix  = tmpl.substring(varEnd + 1);
            boolean fits   = actual.startsWith(prefix) && actual.endsWith(suffix)
                          && actual.length() > prefix.length() + suffix.length();
            if (!fits) return false;
            vars.put(varName, actual.substring(prefix.length(),
                              actual.length() - suffix.length()));
            return true;
        }

        // Caso 3: segmento literal
        return tmpl.equals(actual);
    }

    public HandlerMethod getHandlerMethod() { return handlerMethod; }
    public String        getTemplate()      { return template; }
}

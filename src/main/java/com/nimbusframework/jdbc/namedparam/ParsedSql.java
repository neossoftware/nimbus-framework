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
package com.nimbusframework.jdbc.namedparam;

import java.util.ArrayList;
import java.util.List;

/**
 * Representación de un SQL ya parseado por {@link NamedParameterUtils}: el SQL original
 * más la posición y nombre de cada parámetro nombrado encontrado (incluyendo repeticiones
 * del mismo nombre). Puramente un detalle interno — nunca se expone fuera del paquete.
 */
class ParsedSql {

    private final String originalSql;

    private final List<String> parameterNames    = new ArrayList<>();
    private final List<int[]>  parameterIndexes  = new ArrayList<>();

    private int namedParameterCount;
    private int unnamedParameterCount;

    ParsedSql(String originalSql) {
        this.originalSql = originalSql;
    }

    /** El SQL tal como fue pasado a {@link NamedParameterUtils#parseSqlStatement}, sin modificar. */
    String getOriginalSql() { return originalSql; }

    /** Registra una ocurrencia de {@code :parameterName} encontrada entre {@code [startIndex, endIndex)}. */
    void addNamedParameter(String parameterName, int startIndex, int endIndex) {
        parameterNames.add(parameterName);
        parameterIndexes.add(new int[]{startIndex, endIndex});
    }

    /** Nombres de parámetro en orden de aparición — incluye repeticiones del mismo nombre. */
    List<String> getParameterNames() { return parameterNames; }

    /** {@code [startIndex, endIndex)} en {@link #getOriginalSql()} de la ocurrencia en {@code parameterPosition}. */
    int[] getParameterIndexes(int parameterPosition) { return parameterIndexes.get(parameterPosition); }

    void setNamedParameterCount(int count)   { this.namedParameterCount = count; }
    /** Cantidad de nombres de parámetro DISTINTOS (no cuenta repeticiones). */
    int  getNamedParameterCount()            { return namedParameterCount; }

    void setUnnamedParameterCount(int count) { this.unnamedParameterCount = count; }
    /** Cantidad de "?" tradicionales encontrados — mezclarlos con ":nombre" no está permitido. */
    int  getUnnamedParameterCount()          { return unnamedParameterCount; }
}

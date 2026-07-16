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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helpers de parseo de parámetros nombrados, uso interno de {@link NamedParameterJdbcTemplate}.
 * Adaptado de {@code org.springframework.jdbc.core.namedparam.NamedParameterUtils}, simplificado:
 * soporta {@code :nombre} / {@code &nombre}, saltea comillas/comentarios (para no confundir un
 * ":" dentro de un literal de texto con un parámetro) y expande valores {@code Iterable}/array
 * a múltiples "?" (para cláusulas {@code IN (:lista)}). NO soporta la sintaxis {@code :{x}} ni
 * el escapeo con backslash de Spring — no observados en el uso real que motivó esta clase.
 */
final class NamedParameterUtils {

    private static final String[] START_SKIP = {"'", "\"", "--", "/*"};
    private static final String[] STOP_SKIP  = {"'", "\"", "\n", "*/"};

    /** Caracteres que terminan un nombre de parámetro dentro del SQL. */
    private static final String PARAMETER_SEPARATORS = "\"':&,;()|=+-*%/\\<>^";

    private NamedParameterUtils() { }

    /** Parsea el SQL y ubica los parámetros nombrados ({@code :nombre} / {@code &nombre}). */
    static ParsedSql parseSqlStatement(String sql) {
        Set<String> distinctNames = new HashSet<>();
        char[] statement = sql.toCharArray();
        ParsedSql parsedSql = new ParsedSql(sql);

        int namedParameterCount = 0;
        int unnamedParameterCount = 0;
        int i = 0;
        while (i < statement.length) {
            int skipToPosition = skipCommentsAndQuotes(statement, i);
            if (skipToPosition > i) {
                i = skipToPosition;
                continue;
            }
            if (i >= statement.length) break;

            char c = statement[i];
            if (c == ':' || c == '&') {
                int j = i + 1;
                while (j < statement.length && !isParameterSeparator(statement[j])) {
                    j++;
                }
                if (j - i > 1) {
                    String parameterName = sql.substring(i + 1, j);
                    if (distinctNames.add(parameterName)) {
                        namedParameterCount++;
                    }
                    parsedSql.addNamedParameter(parameterName, i, j);
                }
                i = j - 1;
            } else if (c == '?') {
                unnamedParameterCount++;
            }
            i++;
        }

        if (namedParameterCount > 0 && unnamedParameterCount > 0) {
            throw new IllegalArgumentException("No se pueden mezclar parámetros nombrados (:nombre) con "
                + "placeholders '?' tradicionales. SQL: " + sql);
        }
        parsedSql.setNamedParameterCount(namedParameterCount);
        parsedSql.setUnnamedParameterCount(unnamedParameterCount);
        return parsedSql;
    }

    /** Igual que en Spring: saltea literales entre comillas y comentarios {@code --}/{@code /* *}{@code /}. */
    private static int skipCommentsAndQuotes(char[] statement, int position) {
        for (int i = 0; i < START_SKIP.length; i++) {
            if (statement[position] != START_SKIP[i].charAt(0)) continue;
            boolean match = true;
            for (int j = 1; j < START_SKIP[i].length(); j++) {
                if (position + j >= statement.length || statement[position + j] != START_SKIP[i].charAt(j)) {
                    match = false;
                    break;
                }
            }
            if (!match) continue;

            int offset = START_SKIP[i].length();
            for (int m = position + offset; m < statement.length; m++) {
                if (statement[m] != STOP_SKIP[i].charAt(0)) continue;
                boolean endMatch = true;
                for (int n = 1; n < STOP_SKIP[i].length(); n++) {
                    if (m + n >= statement.length) return statement.length;
                    if (statement[m + n] != STOP_SKIP[i].charAt(n)) {
                        endMatch = false;
                        break;
                    }
                }
                if (endMatch) return m + STOP_SKIP[i].length();
            }
            return statement.length;
        }
        return position;
    }

    private static boolean isParameterSeparator(char c) {
        return Character.isWhitespace(c) || (c < 128 && PARAMETER_SEPARATORS.indexOf(c) >= 0);
    }

    /**
     * Sustituye cada parámetro nombrado por uno o más "?" y arma, en el mismo pasaje, el array
     * de valores en el mismo orden — así el binding posterior (índice a índice) siempre es
     * correcto, incluso cuando un valor {@code Iterable}/array se expande a varios "?"
     * (para {@code IN (:lista)}).
     */
    static SqlAndValues buildSqlAndValues(ParsedSql parsedSql, SqlParameterSource paramSource) {
        String originalSql = parsedSql.getOriginalSql();
        List<String> paramNames = parsedSql.getParameterNames();
        if (paramNames.isEmpty()) {
            return new SqlAndValues(originalSql, new Object[0]);
        }

        StringBuilder sqlBuilder = new StringBuilder(originalSql.length());
        List<Object> values = new ArrayList<>();
        int lastIndex = 0;
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            int[] indexes = parsedSql.getParameterIndexes(i);
            sqlBuilder.append(originalSql, lastIndex, indexes[0]);

            if (!paramSource.hasValue(paramName)) {
                throw new IllegalArgumentException(
                    "No se proveyó valor para el parámetro nombrado '" + paramName + "' en el SQL: " + originalSql);
            }
            List<Object> expanded = new ArrayList<>();
            appendExpanded(expanded, paramSource.getValue(paramName));
            for (int k = 0; k < expanded.size(); k++) {
                if (k > 0) sqlBuilder.append(", ");
                sqlBuilder.append('?');
            }
            values.addAll(expanded);

            lastIndex = indexes[1];
        }
        sqlBuilder.append(originalSql, lastIndex, originalSql.length());
        return new SqlAndValues(sqlBuilder.toString(), values.toArray());
    }

    /**
     * Arma solo el array de valores (sin recalcular el SQL) para una fila de
     * {@code batchUpdate} distinta de la primera — asume que sus parámetros
     * {@code Iterable}/array tienen el mismo tamaño que en la primera fila
     * (misma cantidad de "?" ya sustituidos en el SQL compartido del batch).
     */
    static Object[] buildValueArray(ParsedSql parsedSql, SqlParameterSource paramSource) {
        List<Object> values = new ArrayList<>();
        for (String paramName : parsedSql.getParameterNames()) {
            if (!paramSource.hasValue(paramName)) {
                throw new IllegalArgumentException(
                    "No se proveyó valor para el parámetro nombrado '" + paramName + "'");
            }
            appendExpanded(values, paramSource.getValue(paramName));
        }
        return values.toArray();
    }

    private static void appendExpanded(List<Object> values, Object value) {
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) values.add(item);
        } else if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int k = 0; k < length; k++) values.add(Array.get(value, k));
        } else {
            values.add(value);
        }
    }

    /** SQL con los parámetros ya sustituidos por "?", junto con los valores en el mismo orden. */
    static final class SqlAndValues {
        final String   sql;
        final Object[] values;

        SqlAndValues(String sql, Object[] values) {
            this.sql = sql;
            this.values = values;
        }
    }
}

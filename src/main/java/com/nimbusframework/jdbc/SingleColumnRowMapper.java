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
package com.nimbusframework.jdbc;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link RowMapper} que extrae la primera columna de cada fila y la convierte (best-effort)
 * a {@code requiredType}. Usado por {@code queryForObject(sql, Class)}/{@code queryForList(sql, Class)}
 * tanto en {@link JdbcTemplate} como en {@code NamedParameterJdbcTemplate} — análogo a
 * {@code org.springframework.jdbc.core.SingleColumnRowMapper}.
 */
public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private final Class<T> requiredType;

    public SingleColumnRowMapper(Class<T> requiredType) {
        this.requiredType = requiredType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (T) convertValue(rs.getObject(1), requiredType);
    }

    /** Conversión best-effort entre el tipo devuelto por el driver JDBC y {@code requiredType}. */
    private static Object convertValue(Object value, Class<?> requiredType) {
        if (value == null || requiredType.isInstance(value)) return value;

        String text = value.toString();
        if (requiredType == String.class)                  return text;
        if (requiredType == Long.class    || requiredType == long.class)    return Long.valueOf(text);
        if (requiredType == Integer.class || requiredType == int.class)    return Integer.valueOf(text);
        if (requiredType == Double.class  || requiredType == double.class) return Double.valueOf(text);
        if (requiredType == Float.class   || requiredType == float.class)  return Float.valueOf(text);
        if (requiredType == Boolean.class || requiredType == boolean.class) return Boolean.valueOf(text);
        if (requiredType == BigDecimal.class)               return new BigDecimal(text);
        return value;
    }
}

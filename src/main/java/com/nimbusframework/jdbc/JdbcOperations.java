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

import java.util.List;
import java.util.Map;

/**
 * Operaciones de acceso a datos vía JDBC que expone {@link JdbcTemplate} — análogo a
 * {@code org.springframework.jdbc.core.JdbcOperations}. Programar contra esta interfaz
 * (en vez de contra {@code JdbcTemplate} directamente) permite reemplazar la implementación
 * (por ejemplo, en tests con un stub) sin tocar el código cliente.
 */
public interface JdbcOperations {

    void execute(String sql);

    int update(String sql, Object... args);

    /** Ejecuta un INSERT y puebla {@code generatedKeyHolder} con las claves autogeneradas. */
    int update(String sql, Object[] args, KeyHolder generatedKeyHolder);

    /** Igual que {@link #update(String, Object[], KeyHolder)}, indicando explícitamente qué columnas devolver. */
    int update(String sql, Object[] args, KeyHolder generatedKeyHolder, String[] keyColumnNames);

    int[] batchUpdate(String sql, List<Object[]> batchArgs);

    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args);

    /** Igual que {@link #query(String, RowMapper, Object...)}, con el array de argumentos antes del mapper. */
    <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper);

    <T> T query(String sql, ResultSetExtractor<T> rse);

    <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse);

    void query(String sql, RowCallbackHandler rch, Object... args);

    /** Igual que {@link #query(String, RowCallbackHandler, Object...)}, con el array de argumentos antes del handler. */
    void query(String sql, Object[] args, RowCallbackHandler rch);

    <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args);

    <T> T queryForObject(String sql, Class<T> requiredType, Object... args);

    List<Map<String, Object>> queryForList(String sql, Object... args);

    Map<String, Object> queryForMap(String sql, Object... args);
}

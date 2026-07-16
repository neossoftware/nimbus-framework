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

    /** Ejecuta SQL sin resultado (DDL, o cualquier statement sin parámetros ni retorno). */
    void execute(String sql);

    /** Ejecuta un INSERT/UPDATE/DELETE parametrizado y retorna la cantidad de filas afectadas. */
    int update(String sql, Object... args);

    /** Ejecuta un INSERT y puebla {@code generatedKeyHolder} con las claves autogeneradas. */
    int update(String sql, Object[] args, KeyHolder generatedKeyHolder);

    /** Igual que {@link #update(String, Object[], KeyHolder)}, indicando explícitamente qué columnas devolver. */
    int update(String sql, Object[] args, KeyHolder generatedKeyHolder, String[] keyColumnNames);

    /** Ejecuta el mismo SQL una vez por cada {@code Object[]} de {@code batchArgs}; retorna las filas afectadas por cada uno. */
    int[] batchUpdate(String sql, List<Object[]> batchArgs);

    /** Ejecuta la consulta y mapea cada fila del resultado con {@code rowMapper}. */
    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args);

    /** Igual que {@link #query(String, RowMapper, Object...)}, con el array de argumentos antes del mapper. */
    <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper);

    /** Ejecuta la consulta (sin parámetros) y delega el {@link java.sql.ResultSet} completo a {@code rse}. */
    <T> T query(String sql, ResultSetExtractor<T> rse);

    /** Igual que {@link #query(String, ResultSetExtractor)}, con parámetros posicionales. */
    <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse);

    /** Ejecuta la consulta y llama a {@code rch} por cada fila, sin acumular resultados en una lista. */
    void query(String sql, RowCallbackHandler rch, Object... args);

    /** Igual que {@link #query(String, RowCallbackHandler, Object...)}, con el array de argumentos antes del handler. */
    void query(String sql, Object[] args, RowCallbackHandler rch);

    /** Ejecuta la consulta y retorna la única fila mapeada por {@code rowMapper}. */
    <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args);

    /** Ejecuta la consulta y retorna el valor escalar de la única fila/columna, convertido a {@code requiredType}. */
    <T> T queryForObject(String sql, Class<T> requiredType, Object... args);

    /** Ejecuta la consulta y retorna cada fila como un {@code Map<String, Object>} (columna → valor). */
    List<Map<String, Object>> queryForList(String sql, Object... args);

    /** Ejecuta la consulta y retorna la única fila como un {@code Map<String, Object>} (columna → valor). */
    Map<String, Object> queryForMap(String sql, Object... args);
}

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

import com.nimbusframework.jdbc.JdbcOperations;
import com.nimbusframework.jdbc.KeyHolder;
import com.nimbusframework.jdbc.ResultSetExtractor;
import com.nimbusframework.jdbc.RowCallbackHandler;
import com.nimbusframework.jdbc.RowMapper;

import java.util.List;
import java.util.Map;

/**
 * Operaciones JDBC con parámetros nombrados ({@code :nombre} en vez de "?" posicional) que
 * expone {@link NamedParameterJdbcTemplate} — análogo a
 * {@code org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations}.
 */
public interface NamedParameterJdbcOperations {

    /** Expone el {@link JdbcOperations} clásico envuelto, para los métodos con "?" posicional. */
    JdbcOperations getJdbcOperations();

    /** Ejecuta un INSERT/UPDATE/DELETE con parámetros nombrados y retorna las filas afectadas. */
    int update(String sql, Map<String, ?> paramMap);

    /** Igual que {@link #update(String, Map)}, con una {@link SqlParameterSource} explícita. */
    int update(String sql, SqlParameterSource paramSource);

    /** Ejecuta un INSERT y puebla {@code generatedKeyHolder} con las claves autogeneradas. */
    int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder);

    /** Igual que {@link #update(String, SqlParameterSource, KeyHolder)}, indicando explícitamente qué columnas devolver. */
    int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String[] keyColumnNames);

    /** Ejecuta el mismo SQL una vez por cada elemento de {@code batchValues}; retorna las filas afectadas por cada uno. */
    int[] batchUpdate(String sql, Map<String, ?>[] batchValues);

    /** Igual que {@link #batchUpdate(String, Map[])}, con {@link SqlParameterSource} explícitas. */
    int[] batchUpdate(String sql, SqlParameterSource[] batchArgs);

    /** Ejecuta la consulta con parámetros nombrados y mapea cada fila con {@code rowMapper}. */
    <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper);

    /** Igual que {@link #query(String, Map, RowMapper)}, con una {@link SqlParameterSource} explícita. */
    <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper);

    /** Ejecuta la consulta con parámetros nombrados y delega el {@link java.sql.ResultSet} completo a {@code rse}. */
    <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse);

    /** Igual que {@link #query(String, Map, ResultSetExtractor)}, con una {@link SqlParameterSource} explícita. */
    <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse);

    /** Ejecuta la consulta con parámetros nombrados y llama a {@code rch} por cada fila. */
    void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch);

    /** Igual que {@link #query(String, Map, RowCallbackHandler)}, con una {@link SqlParameterSource} explícita. */
    void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch);

    /** Ejecuta la consulta con parámetros nombrados y retorna la única fila mapeada por {@code rowMapper}. */
    <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper);

    /** Igual que {@link #queryForObject(String, Map, RowMapper)}, con una {@link SqlParameterSource} explícita. */
    <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper);

    /** Ejecuta la consulta con parámetros nombrados y retorna el valor escalar de la única fila/columna. */
    <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType);

    /** Igual que {@link #queryForObject(String, Map, Class)}, con una {@link SqlParameterSource} explícita. */
    <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType);

    /** Ejecuta la consulta con parámetros nombrados y retorna la única fila como {@code Map<String, Object>}. */
    Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap);

    /** Igual que {@link #queryForMap(String, Map)}, con una {@link SqlParameterSource} explícita. */
    Map<String, Object> queryForMap(String sql, SqlParameterSource paramSource);

    /** Ejecuta la consulta con parámetros nombrados y retorna cada fila como valor escalar convertido a {@code elementType}. */
    <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType);

    /** Igual que {@link #queryForList(String, Map, Class)}, con una {@link SqlParameterSource} explícita. */
    <T> List<T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType);

    /** Ejecuta la consulta con parámetros nombrados y retorna cada fila como {@code Map<String, Object>}. */
    List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap);

    /** Igual que {@link #queryForList(String, Map)}, con una {@link SqlParameterSource} explícita. */
    List<Map<String, Object>> queryForList(String sql, SqlParameterSource paramSource);
}

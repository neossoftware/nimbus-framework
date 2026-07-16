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

    int update(String sql, Map<String, ?> paramMap);
    int update(String sql, SqlParameterSource paramSource);

    /** Ejecuta un INSERT y puebla {@code generatedKeyHolder} con las claves autogeneradas. */
    int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder);

    /** Igual que {@link #update(String, SqlParameterSource, KeyHolder)}, indicando explícitamente qué columnas devolver. */
    int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String[] keyColumnNames);

    int[] batchUpdate(String sql, Map<String, ?>[] batchValues);
    int[] batchUpdate(String sql, SqlParameterSource[] batchArgs);

    <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper);
    <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper);

    <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse);
    <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse);

    void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch);
    void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch);

    <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper);
    <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper);

    <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType);
    <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType);

    Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap);
    Map<String, Object> queryForMap(String sql, SqlParameterSource paramSource);

    <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType);
    <T> List<T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType);

    List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap);
    List<Map<String, Object>> queryForList(String sql, SqlParameterSource paramSource);
}

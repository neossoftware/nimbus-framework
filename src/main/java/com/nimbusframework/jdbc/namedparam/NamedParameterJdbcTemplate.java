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
import com.nimbusframework.jdbc.JdbcTemplate;
import com.nimbusframework.jdbc.ResultSetExtractor;
import com.nimbusframework.jdbc.RowMapper;
import com.nimbusframework.jdbc.SingleColumnRowMapper;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Acceso a SQL con parámetros nombrados ({@code :nombre} en vez de "?" posicional), inspirado
 * en {@code org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate}. Envuelve un
 * {@link JdbcOperations} (típicamente un {@link JdbcTemplate}): resuelve los nombres a "?" y
 * arma el array de valores en el orden correcto, y delega la ejecución real.
 *
 * Soporta expandir un valor {@code Iterable}/array a varios "?" — por ejemplo
 * {@code WHERE id IN (:ids)} con {@code ids} un {@code List<Integer>} — pero NO la sintaxis
 * {@code :{x}} de Spring ni su escapeo con backslash (no había uso real de esos casos).
 *
 * Se instancia como cualquier otro bean XML:
 * <pre>
 *   {@code <bean id="namedParameterJdbcTemplate" class="com.nimbusframework.jdbc.namedparam.NamedParameterJdbcTemplate">}
 *     {@code <property name="jdbcOperations" ref="jdbcTemplate"/>}
 *   {@code </bean>}
 * </pre>
 * o directamente sobre un {@code DataSource} (crea internamente un {@code JdbcTemplate}):
 * <pre>
 *   {@code <bean id="namedParameterJdbcTemplate" class="com.nimbusframework.jdbc.namedparam.NamedParameterJdbcTemplate">}
 *     {@code <property name="dataSource" ref="dataSource"/>}
 *   {@code </bean>}
 * </pre>
 */
public class NamedParameterJdbcTemplate implements NamedParameterJdbcOperations {

    private JdbcOperations jdbcOperations;

    /** SQL original -> parseo — evita re-parsear el mismo SQL (típicamente una constante) en cada llamada. */
    private final Map<String, ParsedSql> parsedSqlCache = new ConcurrentHashMap<>();

    public NamedParameterJdbcTemplate() { }

    public NamedParameterJdbcTemplate(DataSource dataSource) {
        this.jdbcOperations = new JdbcTemplate(dataSource);
    }

    public NamedParameterJdbcTemplate(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public void setDataSource(DataSource dataSource)         { this.jdbcOperations = new JdbcTemplate(dataSource); }
    public void setJdbcOperations(JdbcOperations jdbcOperations) { this.jdbcOperations = jdbcOperations; }

    @Override
    public JdbcOperations getJdbcOperations() { return jdbcOperations; }

    // -----------------------------------------------------------------------
    // Sin resultado
    // -----------------------------------------------------------------------

    @Override
    public int update(String sql, Map<String, ?> paramMap) {
        return update(sql, new MapSqlParameterSource(paramMap));
    }

    @Override
    public int update(String sql, SqlParameterSource paramSource) {
        NamedParameterUtils.SqlAndValues sv = buildSqlAndValues(sql, paramSource);
        return jdbcOperations.update(sv.sql, sv.values);
    }

    @Override
    public int[] batchUpdate(String sql, Map<String, ?>[] batchValues) {
        SqlParameterSource[] sources = new SqlParameterSource[batchValues.length];
        for (int i = 0; i < batchValues.length; i++) {
            sources[i] = new MapSqlParameterSource(batchValues[i]);
        }
        return batchUpdate(sql, sources);
    }

    @Override
    public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) {
        if (batchArgs.length == 0) return new int[0];

        ParsedSql parsedSql = getParsedSql(sql);
        NamedParameterUtils.SqlAndValues first = NamedParameterUtils.buildSqlAndValues(parsedSql, batchArgs[0]);

        List<Object[]> batchValues = new ArrayList<>(batchArgs.length);
        batchValues.add(first.values);
        for (int i = 1; i < batchArgs.length; i++) {
            batchValues.add(NamedParameterUtils.buildValueArray(parsedSql, batchArgs[i]));
        }
        return jdbcOperations.batchUpdate(first.sql, batchValues);
    }

    // -----------------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------------

    @Override
    public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) {
        return query(sql, new MapSqlParameterSource(paramMap), rowMapper);
    }

    @Override
    public <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) {
        NamedParameterUtils.SqlAndValues sv = buildSqlAndValues(sql, paramSource);
        return jdbcOperations.query(sv.sql, rowMapper, sv.values);
    }

    @Override
    public <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse) {
        return query(sql, new MapSqlParameterSource(paramMap), rse);
    }

    @Override
    public <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse) {
        NamedParameterUtils.SqlAndValues sv = buildSqlAndValues(sql, paramSource);
        return jdbcOperations.query(sv.sql, sv.values, rse);
    }

    @Override
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) {
        return queryForObject(sql, new MapSqlParameterSource(paramMap), rowMapper);
    }

    @Override
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) {
        NamedParameterUtils.SqlAndValues sv = buildSqlAndValues(sql, paramSource);
        return jdbcOperations.queryForObject(sv.sql, rowMapper, sv.values);
    }

    @Override
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType) {
        return queryForObject(sql, new MapSqlParameterSource(paramMap), requiredType);
    }

    @Override
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType) {
        return queryForObject(sql, paramSource, new SingleColumnRowMapper<>(requiredType));
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) {
        return queryForMap(sql, new MapSqlParameterSource(paramMap));
    }

    @Override
    public Map<String, Object> queryForMap(String sql, SqlParameterSource paramSource) {
        NamedParameterUtils.SqlAndValues sv = buildSqlAndValues(sql, paramSource);
        return jdbcOperations.queryForMap(sv.sql, sv.values);
    }

    @Override
    public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType) {
        return queryForList(sql, new MapSqlParameterSource(paramMap), elementType);
    }

    @Override
    public <T> List<T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType) {
        return query(sql, paramSource, new SingleColumnRowMapper<>(elementType));
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap) {
        return queryForList(sql, new MapSqlParameterSource(paramMap));
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, SqlParameterSource paramSource) {
        NamedParameterUtils.SqlAndValues sv = buildSqlAndValues(sql, paramSource);
        return jdbcOperations.queryForList(sv.sql, sv.values);
    }

    // -----------------------------------------------------------------------
    // Helpers internos
    // -----------------------------------------------------------------------

    private NamedParameterUtils.SqlAndValues buildSqlAndValues(String sql, SqlParameterSource paramSource) {
        return NamedParameterUtils.buildSqlAndValues(getParsedSql(sql), paramSource);
    }

    private ParsedSql getParsedSql(String sql) {
        return parsedSqlCache.computeIfAbsent(sql, NamedParameterUtils::parseSqlStatement);
    }
}

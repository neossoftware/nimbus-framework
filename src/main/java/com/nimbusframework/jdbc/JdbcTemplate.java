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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso directo a SQL vía JDBC, inspirado en {@code org.springframework.jdbc.core.JdbcTemplate}
 * pero acotado al núcleo de operaciones (sin CallableStatement, SqlRowSet ni streams) —
 * una alternativa liviana a la capa JPA de Nimbus para cuando esta última es demasiado o
 * no hay proveedor JPA disponible en el contenedor destino.
 *
 * Cada operación abre y cierra su propia {@link Connection} (autocommit); NO participa
 * de las transacciones de @Transactional/EntityManagerHolder — esas solo gestionan el
 * EntityManager de la capa JPA.
 *
 * Se instancia como cualquier otro bean XML, con el {@link DataSource} inyectado vía
 * {@code <property name="dataSource" ref="...">}:
 * <pre>
 *   {@code <bean id="jdbcTemplate" class="com.nimbusframework.jdbc.JdbcTemplate">}
 *     {@code <property name="dataSource" ref="dataSource"/>}
 *   {@code </bean>}
 * </pre>
 */
public class JdbcTemplate implements JdbcOperations {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private DataSource dataSource;

    public JdbcTemplate() { }

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) { this.dataSource = dataSource; }
    public DataSource getDataSource()                { return dataSource; }

    // -----------------------------------------------------------------------
    // Sin resultado / DDL
    // -----------------------------------------------------------------------

    @Override
    public void execute(String sql) {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw translate("execute", sql, e);
        }
    }

    @Override
    public int update(String sql, Object... args) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setParameters(ps, args);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw translate("update", sql, e);
        }
    }

    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] args : batchArgs) {
                setParameters(ps, args);
                ps.addBatch();
            }
            return ps.executeBatch();
        } catch (SQLException e) {
            throw translate("batchUpdate", sql, e);
        }
    }

    // -----------------------------------------------------------------------
    // Consultas
    // -----------------------------------------------------------------------

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setParameters(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        } catch (SQLException e) {
            throw translate("query", sql, e);
        }
    }

    @Override
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
        return query(sql, rowMapper, args);
    }

    @Override
    public <T> T query(String sql, ResultSetExtractor<T> rse) {
        return query(sql, EMPTY_ARGS, rse);
    }

    @Override
    public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setParameters(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                return rse.extractData(rs);
            }
        } catch (SQLException e) {
            throw translate("query", sql, e);
        }
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return singleResult(results);
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        return queryForObject(sql, new SingleColumnRowMapper<>(requiredType), args);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        return query(sql, (rs, rowNum) -> extractRow(rs), args);
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... args) {
        return singleResult(queryForList(sql, args));
    }

    // -----------------------------------------------------------------------
    // Helpers internos
    // -----------------------------------------------------------------------

    private static <T> T singleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.get(0);
    }

    private static void setParameters(PreparedStatement ps, Object[] args) throws SQLException {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private static Map<String, Object> extractRow(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        Map<String, Object> row = new LinkedHashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            row.put(md.getColumnLabel(i), rs.getObject(i));
        }
        return row;
    }

    private static DataAccessException translate(String operation, String sql, SQLException e) {
        return new DataAccessException(operation + " falló para SQL [" + sql + "]: " + e.getMessage(), e);
    }
}

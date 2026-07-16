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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Convierte una fila del {@link ResultSet} actual en un objeto de dominio.
 * {@link JdbcTemplate} llama a {@code mapRow} por cada fila, sin avanzar el
 * cursor (eso lo hace el propio JdbcTemplate) — la implementación solo lee
 * las columnas de la fila posicionada.
 */
@FunctionalInterface
public interface RowMapper<T> {

    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}

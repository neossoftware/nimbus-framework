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
 * Procesa una fila del {@link ResultSet} sin devolver nada — a diferencia de {@link RowMapper},
 * no acumula resultados en una lista (útil para recorrer un resultado grande fila por fila,
 * por ejemplo para exportar a un archivo). Análogo a
 * {@code org.springframework.jdbc.core.RowCallbackHandler}.
 */
@FunctionalInterface
public interface RowCallbackHandler {

    void processRow(ResultSet rs) throws SQLException;
}

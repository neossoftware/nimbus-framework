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
 * Procesa el {@link ResultSet} completo de una consulta y produce un único resultado —
 * a diferencia de {@link RowMapper}, que mapea fila por fila a una lista, esta interfaz
 * recibe el ResultSet entero y es responsable de recorrerlo ella misma (útil para
 * resultados que no son "una lista de filas", por ejemplo un agregado en un Map).
 */
@FunctionalInterface
public interface ResultSetExtractor<T> {

    /** Recorre {@code rs} completo (llamando a {@code rs.next()} las veces que haga falta) y arma el resultado. */
    T extractData(ResultSet rs) throws SQLException;
}

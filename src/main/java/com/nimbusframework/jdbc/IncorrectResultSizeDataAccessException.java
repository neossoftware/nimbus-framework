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

/**
 * Se lanza desde {@link JdbcTemplate#queryForObject} / {@code queryForMap} cuando
 * la consulta esperaba exactamente una fila y devolvió más de una.
 */
public class IncorrectResultSizeDataAccessException extends DataAccessException {

    /**
     * @param expectedSize cantidad de filas que se esperaban (típicamente 1).
     * @param actualSize   cantidad de filas realmente devueltas.
     */
    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super("Resultado incorrecto: se esperaba(n) " + expectedSize + " fila(s) y se obtuvieron " + actualSize, null);
    }
}

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
 * Envuelve cualquier {@link java.sql.SQLException} que ocurra dentro de un
 * {@link JdbcTemplate} en una excepción unchecked, para no obligar al código
 * cliente a declarar throws SQLException en cada método.
 *
 * Capturar con @ExceptionHandler(DataAccessException.class) en un @ControllerAdvice
 * si se quiere manejar errores de acceso a datos de forma centralizada.
 */
public class DataAccessException extends RuntimeException {

    /** @param cause la {@link java.sql.SQLException} original. */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

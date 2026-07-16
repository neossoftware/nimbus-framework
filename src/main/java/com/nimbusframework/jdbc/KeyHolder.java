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
 * Recibe las claves autogeneradas (ej. un ID autoincremental) tras un
 * {@code update(sql, args, keyHolder)} de inserción. Análogo a
 * {@code org.springframework.jdbc.support.KeyHolder}. {@link JdbcTemplate} puebla
 * {@link #getKeyList()} directamente (una fila por clave generada, columna→valor).
 */
public interface KeyHolder {

    /**
     * La única clave generada, cuando hay exactamente una fila con una sola columna.
     * @throws EmptyResultDataAccessException si no se generó ninguna clave
     * @throws IncorrectResultSizeDataAccessException si hay más de una fila o más de una columna
     */
    Number getKey();

    /** La primera fila de claves generadas, o {@code null} si no se generó ninguna. */
    Map<String, Object> getKeys();

    /** Todas las filas de claves generadas (más de una fila puede darse en batch inserts). */
    List<Map<String, Object>> getKeyList();
}

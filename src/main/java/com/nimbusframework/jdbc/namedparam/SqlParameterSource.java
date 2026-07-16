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

/**
 * Fuente de valores para los parámetros nombrados de {@link NamedParameterJdbcTemplate}
 * (":paramName" en el SQL) — análoga a {@code org.springframework.jdbc.core.namedparam.SqlParameterSource}.
 * A diferencia de la de Spring, no expone tipo SQL/nombre de tipo: Nimbus siempre bindea
 * los valores vía {@code PreparedStatement.setObject(...)}, sin necesitar esa metadata.
 */
public interface SqlParameterSource {

    /** @return true si hay un valor asignado para {@code paramName}. */
    boolean hasValue(String paramName);

    /** @throws IllegalArgumentException si no hay valor para {@code paramName} (ver {@link #hasValue}). */
    Object getValue(String paramName);
}

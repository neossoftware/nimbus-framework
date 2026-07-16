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

import java.util.HashMap;
import java.util.Map;

/**
 * {@link SqlParameterSource} respaldada por un {@link Map}. Análoga a
 * {@code org.springframework.jdbc.core.namedparam.MapSqlParameterSource}.
 * {@link NamedParameterJdbcTemplate} envuelve automáticamente un {@code Map<String, ?>}
 * en una instancia de esta clase — normalmente no hace falta instanciarla a mano salvo
 * que se quiera usar {@link #addValue} de forma fluida.
 */
public class MapSqlParameterSource implements SqlParameterSource {

    private final Map<String, Object> values = new HashMap<>();

    /** Crea una fuente vacía — poblala con {@link #addValue}. */
    public MapSqlParameterSource() { }

    /** Crea una fuente con los valores de {@code values} ya cargados. */
    public MapSqlParameterSource(Map<String, ?> values) {
        this.values.putAll(values);
    }

    /** Agrega/reemplaza el valor de {@code paramName} y retorna {@code this} (uso fluido). */
    public MapSqlParameterSource addValue(String paramName, Object value) {
        this.values.put(paramName, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasValue(String paramName) {
        return values.containsKey(paramName);
    }

    /** {@inheritDoc} */
    @Override
    public Object getValue(String paramName) {
        if (!hasValue(paramName)) {
            throw new IllegalArgumentException("No se proveyó valor para el parámetro con nombre '" + paramName + "'");
        }
        return values.get(paramName);
    }
}

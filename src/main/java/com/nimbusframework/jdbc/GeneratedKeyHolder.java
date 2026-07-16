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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementación estándar de {@link KeyHolder}, análoga a
 * {@code org.springframework.jdbc.support.GeneratedKeyHolder}.
 * <pre>
 *   KeyHolder keyHolder = new GeneratedKeyHolder();
 *   jdbcTemplate.update("INSERT INTO cursos (nombre) VALUES (?)", new Object[]{"Java"}, keyHolder);
 *   long id = keyHolder.getKey().longValue();
 * </pre>
 */
public class GeneratedKeyHolder implements KeyHolder {

    private final List<Map<String, Object>> keyList = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public Number getKey() {
        if (keyList.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (keyList.size() > 1 || keyList.get(0).size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1, keyList.size());
        }
        return (Number) keyList.get(0).values().iterator().next();
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getKeys() {
        return keyList.isEmpty() ? null : keyList.get(0);
    }

    /** {@inheritDoc} */
    @Override
    public List<Map<String, Object>> getKeyList() {
        return keyList;
    }
}

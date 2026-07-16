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
package com.nimbusframework.repository;

/**
 * Especificación de ordenamiento: campo + dirección.
 *
 * <pre>
 *   Sort sort = Sort.by("nombre").ascending();
 *   Sort sort = Sort.by("duracionHoras").descending();
 * </pre>
 */
public class Sort {

    public enum Direction { ASC, DESC }

    private final String    field;
    private final Direction direction;

    private Sort(String field, Direction direction) {
        this.field     = field;
        this.direction = direction;
    }

    public static Sort by(String field) {
        return new Sort(field, Direction.ASC);
    }

    public Sort ascending()  { return new Sort(field, Direction.ASC); }
    public Sort descending() { return new Sort(field, Direction.DESC); }

    public String    getField()     { return field; }
    public Direction getDirection() { return direction; }
    public boolean   isSorted()     { return field != null && !field.isEmpty(); }

    /** Sort vacío — sin ordenamiento explícito. */
    public static final Sort UNSORTED = new Sort("", Direction.ASC) {
        @Override public boolean isSorted() { return false; }
    };
}

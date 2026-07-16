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

import java.util.List;

/** Implementación concreta de {@link Page}. */
public class PageImpl<T> implements Page<T> {

    private final List<T>  content;
    private final Pageable pageable;
    private final long     totalElements;

    /**
     * @param content       el contenido de esta página.
     * @param pageable      la paginación usada para obtenerla.
     * @param totalElements el total de registros en la BD (todas las páginas).
     */
    public PageImpl(List<T> content, Pageable pageable, long totalElements) {
        this.content       = content;
        this.pageable      = pageable;
        this.totalElements = totalElements;
    }

    /** @return el contenido de esta página. */
    @Override public List<T> getContent()       { return content; }
    /** @return el tamaño de página. */
    @Override public int     getSize()          { return pageable.getPageSize(); }
    /** @return el total de registros en la BD (todas las páginas). */
    @Override public long    getTotalElements() { return totalElements; }

    /** @return el número de página actual (1-based). */
    @Override
    public int getNumber() {
        return pageable.getPageNumber() + 1; // convertir a 1-based para la vista
    }

    /** @return el total de páginas. */
    @Override
    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageable.getPageSize());
    }

    /** @return true si esta es la primera página. */
    @Override public boolean isFirst()       { return pageable.getPageNumber() == 0; }
    /** @return true si esta es la última página. */
    @Override public boolean isLast()        { return getNumber() >= getTotalPages(); }
    /** @return true si hay una página siguiente. */
    @Override public boolean hasNext()       { return !isLast(); }
    /** @return true si hay una página anterior. */
    @Override public boolean hasPrevious()   { return !isFirst(); }
}

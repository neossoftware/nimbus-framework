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
 * Implementación de {@link Pageable}.
 *
 * <pre>
 *   PageRequest.of(1, 5)                                 // página 1, 5 por página, sin orden
 *   PageRequest.of(2, 10, Sort.by("nombre").ascending()) // página 2, 10 por página, por nombre ASC
 * </pre>
 *
 * El número de página es 1-based en la API pública; internamente se almacena 0-based
 * para que getOffset() = (pageNum-1) * pageSize.
 */
public class PageRequest implements Pageable {

    private final int  pageNumber; // 0-based
    private final int  pageSize;
    private final Sort sort;

    private PageRequest(int pageNumber, int pageSize, Sort sort) {
        if (pageNumber < 0) throw new IllegalArgumentException("El número de página no puede ser negativo");
        if (pageSize    < 1) throw new IllegalArgumentException("El tamaño de página debe ser >= 1");
        this.pageNumber = pageNumber;
        this.pageSize   = pageSize;
        this.sort       = sort != null ? sort : Sort.UNSORTED;
    }

    /** @param pageNum número de página (1-based) */
    public static PageRequest of(int pageNum, int pageSize) {
        return new PageRequest(pageNum - 1, pageSize, Sort.UNSORTED);
    }

    /** @param pageNum número de página (1-based) */
    public static PageRequest of(int pageNum, int pageSize, Sort sort) {
        return new PageRequest(pageNum - 1, pageSize, sort);
    }

    /** @return el número de página (0-based internamente). */
    @Override public int  getPageNumber() { return pageNumber; }
    /** @return el tamaño de página. */
    @Override public int  getPageSize()   { return pageSize; }
    /** @return el ordenamiento asociado, o {@link Sort#UNSORTED} si no se especificó ninguno. */
    @Override public Sort getSort()       { return sort; }
}

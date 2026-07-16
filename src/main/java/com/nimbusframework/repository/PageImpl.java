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

    public PageImpl(List<T> content, Pageable pageable, long totalElements) {
        this.content       = content;
        this.pageable      = pageable;
        this.totalElements = totalElements;
    }

    @Override public List<T> getContent()       { return content; }
    @Override public int     getSize()          { return pageable.getPageSize(); }
    @Override public long    getTotalElements() { return totalElements; }

    @Override
    public int getNumber() {
        return pageable.getPageNumber() + 1; // convertir a 1-based para la vista
    }

    @Override
    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageable.getPageSize());
    }

    @Override public boolean isFirst()       { return pageable.getPageNumber() == 0; }
    @Override public boolean isLast()        { return getNumber() >= getTotalPages(); }
    @Override public boolean hasNext()       { return !isLast(); }
    @Override public boolean hasPrevious()   { return !isFirst(); }
}

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

/**
 * Página de resultados — contiene el contenido más metadatos de paginación.
 */
public interface Page<T> {
    /** @return el contenido de esta página. */
    List<T> getContent();
    /** @return el número de página actual (1-based). */
    int     getNumber();
    /** @return el tamaño de página. */
    int     getSize();
    /** @return el total de registros en la BD (todas las páginas). */
    long    getTotalElements();
    /** @return el total de páginas. */
    int     getTotalPages();
    /** @return true si esta es la primera página. */
    boolean isFirst();
    /** @return true si esta es la última página. */
    boolean isLast();
    /** @return true si hay una página siguiente. */
    boolean hasNext();
    /** @return true si hay una página anterior. */
    boolean hasPrevious();
}

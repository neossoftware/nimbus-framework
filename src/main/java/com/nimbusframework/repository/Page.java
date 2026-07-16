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
    List<T> getContent();
    int     getNumber();        // número de página actual (1-based)
    int     getSize();          // tamaño de página
    long    getTotalElements(); // total de registros en la BD
    int     getTotalPages();    // total de páginas
    boolean isFirst();
    boolean isLast();
    boolean hasNext();
    boolean hasPrevious();
}

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
 * Parámetros de paginación: página, tamaño y ordenamiento.
 * Crear instancias con {@link PageRequest#of(int, int)} o {@link PageRequest#of(int, int, Sort)}.
 */
public interface Pageable {
    /** Número de página (0-based internamente; la API pública es 1-based en PageRequest). */
    int  getPageNumber();
    int  getPageSize();
    Sort getSort();
    /** Offset para la query JPA: pageNumber * pageSize. */
    default int getOffset() { return getPageNumber() * getPageSize(); }
}

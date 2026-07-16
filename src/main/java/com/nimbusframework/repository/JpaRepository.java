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
import java.util.Optional;

/**
 * Repositorio genérico de Nimbus — equivalente a JpaRepository de Spring Data.
 *
 * Uso:
 * <pre>
 *   {@literal @}Repository
 *   public interface CursoRepository extends JpaRepository{@literal <}Curso, Integer{@literal >} { }
 * </pre>
 *
 * El framework detecta la interfaz en el component-scan, crea un proxy JDK
 * respaldado por {@link SimpleJpaRepository} y lo registra como bean.
 * No hace falta ninguna implementación manual.
 */
public interface JpaRepository<T, ID> {

    /** Retorna todas las entidades. */
    List<T> findAll();

    /** Retorna la entidad con el id dado, o {@code Optional.empty()} si no existe. */
    Optional<T> findById(ID id);

    /**
     * Persiste o actualiza la entidad.
     * Si el id es null/0 → {@code persist} (INSERT).
     * Si el id tiene valor → {@code merge}  (UPDATE).
     */
    T save(T entity);

    /** Elimina la entidad con el id dado (no lanza excepción si no existe). */
    void deleteById(ID id);

    /** Número total de registros en la tabla. */
    long count();

    /** Retorna true si existe una entidad con ese id. */
    boolean existsById(ID id);

    /** Retorna una página de resultados según el {@link Pageable} indicado. */
    Page<T> findAll(Pageable pageable);
}

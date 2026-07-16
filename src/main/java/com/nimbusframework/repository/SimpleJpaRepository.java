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

import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA de {@link JpaRepository}.
 * El EntityManager recibido es el proxy de {@link com.nimbusframework.jpa.EntityManagerHolder}
 * — cada llamada delega al EM activo en el hilo.
 */
public class SimpleJpaRepository<T, ID> {

    private final Class<T>      entityClass;
    private final EntityManager em;

    /**
     * @param entityClass la clase de la entidad manejada.
     * @param em          el EntityManager a usar (típicamente el proxy de {@link com.nimbusframework.jpa.EntityManagerHolder}).
     */
    public SimpleJpaRepository(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em          = em;
    }

    /** Retorna todas las entidades. */
    public List<T> findAll() {
        return em.createQuery(
                "SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList();
    }

    /** Retorna la entidad con el id dado, o {@code Optional.empty()} si no existe. */
    public Optional<T> findById(Object id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    /**
     * Persiste o actualiza la entidad.
     * Si el id es null/0 → {@code persist} (INSERT).
     * Si el id tiene valor → {@code merge}  (UPDATE).
     */
    public T save(T entity) {
        Object idValue = getIdValue(entity);
        boolean isNew = idValue == null
            || (idValue instanceof Number && ((Number) idValue).longValue() == 0L);
        if (isNew) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    /** Elimina la entidad con el id dado (no lanza excepción si no existe). */
    public void deleteById(Object id) {
        T entity = em.find(entityClass, id);
        if (entity != null) em.remove(entity);
    }

    /** Número total de registros en la tabla. */
    public long count() {
        return em.createQuery(
                "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }

    /** Retorna true si existe una entidad con ese id. */
    public boolean existsById(Object id) {
        return findById(id).isPresent();
    }

    /** Retorna una página de resultados según el {@link Pageable} indicado. */
    public Page<T> findAll(Pageable pageable) {
        String entity = entityClass.getSimpleName();

        // Query de conteo
        long total = em.createQuery(
                "SELECT COUNT(e) FROM " + entity + " e", Long.class)
                .getSingleResult();

        // Query de datos con ordenamiento opcional
        StringBuilder jpql = new StringBuilder("SELECT e FROM " + entity + " e");
        Sort sort = pageable.getSort();
        if (sort != null && sort.isSorted()) {
            jpql.append(" ORDER BY e.")
                .append(sort.getField())
                .append(" ")
                .append(sort.getDirection().name());
        }

        List<T> content = em.createQuery(jpql.toString(), entityClass)
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    // -----------------------------------------------------------------------

    private Object getIdValue(T entity) {
        for (Field field : getAllFields(entity.getClass())) {
            if (field.isAnnotationPresent(Id.class)
                    || field.isAnnotationPresent(javax.persistence.Id.class)) {
                field.setAccessible(true);
                try { return field.get(entity); } catch (IllegalAccessException e) { return null; }
            }
        }
        return null;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}

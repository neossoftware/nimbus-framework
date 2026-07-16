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
package com.nimbusframework.jpa;

import javax.persistence.EntityManager;

/**
 * ThreadLocal que mantiene el EntityManager activo para el hilo actual.
 * TransactionalProxyFactory lo puebla antes de cada método @Transactional
 * y lo limpia al terminar.
 */
public class EntityManagerHolder {

    private static final ThreadLocal<EntityManager> holder = new ThreadLocal<>();

    /** @return el EntityManager activo en el hilo actual, o {@code null} si no hay ninguno. */
    public static EntityManager get()                { return holder.get(); }
    /** Registra {@code em} como el EntityManager activo del hilo actual. */
    public static void          set(EntityManager em){ holder.set(em); }
    /** Limpia el EntityManager del hilo actual (llamar siempre en un finally). */
    public static void          remove()             { holder.remove(); }
}

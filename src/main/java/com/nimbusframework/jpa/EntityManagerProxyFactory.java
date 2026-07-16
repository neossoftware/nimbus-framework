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
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Crea un proxy de EntityManager que delega cada llamada al EntityManager
 * del hilo actual (EntityManagerHolder). Esto permite inyectar una sola
 * instancia proxy en los @Repository y que cada request use su propio EM.
 */
public class EntityManagerProxyFactory {

    public static EntityManager createProxy(EntityManagerFactory emf) {
        return (EntityManager) Proxy.newProxyInstance(
            EntityManager.class.getClassLoader(),
            new Class<?>[]{ EntityManager.class },
            (proxy, method, args) -> {
                EntityManager em = EntityManagerHolder.get();
                if (em == null) {
                    throw new IllegalStateException(
                        "No hay EntityManager en el hilo actual. " +
                        "Asegúrate de llamar desde un método @Transactional.");
                }
                try {
                    return method.invoke(em, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }
}

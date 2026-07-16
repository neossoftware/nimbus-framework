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

import com.nimbusframework.jpa.EntityManagerHolder;
import com.nimbusframework.jpa.EntityManagerProxyFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

/**
 * Crea un proxy JDK para una interfaz de repositorio que extiende {@link JpaRepository}.
 *
 * El proxy:
 * <ol>
 *   <li>Delega cada método a {@link SimpleJpaRepository}.</li>
 *   <li>Gestiona la transacción automáticamente si no hay una activa en el hilo
 *       (auto-transaccional, igual que SimpleJpaRepository de Spring Data).</li>
 * </ol>
 */
public class RepositoryProxyFactory {

    private static final Logger log = Logger.getLogger(RepositoryProxyFactory.class.getName());

    public static Object createProxy(
            Class<?> repositoryInterface,
            Class<?> entityClass,
            EntityManagerFactory emf) {

        // EM proxy → delega a EntityManagerHolder (ThreadLocal)
        EntityManager emProxy = EntityManagerProxyFactory.createProxy(emf);
        @SuppressWarnings("unchecked")
        SimpleJpaRepository<Object, Object> impl =
            new SimpleJpaRepository<>((Class<Object>) entityClass, emProxy);

        log.info("Creando proxy JpaRepository: " + repositoryInterface.getSimpleName()
            + "<" + entityClass.getSimpleName() + ">");

        return Proxy.newProxyInstance(
            repositoryInterface.getClassLoader(),
            new Class<?>[]{ repositoryInterface },
            new Handler(impl, emf)
        );
    }

    // -----------------------------------------------------------------------

    private static class Handler implements InvocationHandler {

        private final SimpleJpaRepository<Object, Object> impl;
        private final EntityManagerFactory emf;

        Handler(SimpleJpaRepository<Object, Object> impl, EntityManagerFactory emf) {
            this.impl = impl;
            this.emf  = emf;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Encontrar el método equivalente en SimpleJpaRepository
            Method implMethod = resolveImplMethod(method);

            // Si ya hay una transacción activa (llamada desde @Transactional), participar en ella
            if (EntityManagerHolder.get() != null) {
                return call(implMethod, args);
            }

            // Sin transacción activa → crear una propia (auto-transaccional)
            EntityManager em = emf.createEntityManager();
            EntityManagerHolder.set(em);
            try {
                em.getTransaction().begin();
                Object result = call(implMethod, args);
                em.getTransaction().commit();
                return result;
            } catch (Throwable t) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw t;
            } finally {
                EntityManagerHolder.remove();
                em.close();
            }
        }

        private Object call(Method implMethod, Object[] args) throws Throwable {
            try {
                return implMethod.invoke(impl, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private Method resolveImplMethod(Method interfaceMethod) throws NoSuchMethodException {
            // La mayoría de los tipos de parámetros se resuelven directamente.
            // Los genéricos (T, ID) borran a Object en bytecode.
            try {
                return SimpleJpaRepository.class.getMethod(
                    interfaceMethod.getName(),
                    interfaceMethod.getParameterTypes());
            } catch (NoSuchMethodException e) {
                // Fallback: buscar por nombre si los tipos genéricos borraron diferente
                for (Method m : SimpleJpaRepository.class.getMethods()) {
                    if (m.getName().equals(interfaceMethod.getName())
                        && m.getParameterCount() == interfaceMethod.getParameterCount()) {
                        return m;
                    }
                }
                throw new UnsupportedOperationException(
                    "Método no soportado en JpaRepository: " + interfaceMethod.getName());
            }
        }
    }
}

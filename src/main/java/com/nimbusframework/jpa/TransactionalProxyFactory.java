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

import com.nimbusframework.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

/**
 * Envuelve un bean con @Transactional en un JDK proxy.
 *
 * Comportamiento por llamada:
 *  - Si no hay @Transactional en el método ni en la clase → pasa directo.
 *  - Si ya hay un EntityManager en el ThreadLocal (transacción anidada) → reutiliza.
 *  - En caso contrario → crea EM, begin(), invoca, commit(). Rollback si hay excepción.
 *
 * Requisito: el bean debe implementar al menos una interfaz (JDK proxy).
 */
public class TransactionalProxyFactory {

    private static final Logger log = Logger.getLogger(TransactionalProxyFactory.class.getName());

    /**
     * Envuelve {@code target} en un proxy que gestiona la transacción para los métodos
     * marcados con @Transactional. Si {@code target} no implementa ninguna interfaz,
     * se retorna sin envolver (con un warning) porque un JDK proxy no es posible.
     */
    public static Object createProxy(Object target, EntityManagerFactory emf) {
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0) {
            log.warning("@Transactional en " + target.getClass().getSimpleName()
                + " ignorado — la clase no implementa interfaces (JDK proxy requiere al menos una).");
            return target;
        }
        return Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            interfaces,
            new Handler(target, emf)
        );
    }

    private static class Handler implements InvocationHandler {

        private final Object target;
        private final EntityManagerFactory emf;

        Handler(Object target, EntityManagerFactory emf) {
            this.target = target;
            this.emf    = emf;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Buscar el método real en la clase target (puede tener @Transactional)
            Method targetMethod;
            try {
                targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return call(method, args);
            }

            boolean tx = targetMethod.isAnnotationPresent(Transactional.class)
                      || target.getClass().isAnnotationPresent(Transactional.class);

            if (!tx) return call(method, args);

            // Transacción anidada — participar en la existente
            if (EntityManagerHolder.get() != null) return call(method, args);

            EntityManager em = emf.createEntityManager();
            EntityManagerHolder.set(em);
            try {
                em.getTransaction().begin();
                Object result = call(method, args);
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

        private Object call(Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}

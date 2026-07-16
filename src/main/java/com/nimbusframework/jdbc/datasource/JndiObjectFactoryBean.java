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
package com.nimbusframework.jdbc.datasource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link DataSource} que resuelve el DataSource real vía JNDI — el caso típico en
 * Tomcat/IBM WAS, donde el contenedor gestiona el pool y lo expone bajo un nombre
 * como {@code java:comp/env/jdbc/miDataSource}.
 *
 * Inspirada en {@code org.springframework.jndi.JndiObjectFactoryBean} de Spring, pero
 * adaptada: Nimbus no tiene el concepto de {@code FactoryBean} (un bean cuyo
 * {@code getObject()} se "desempaqueta" antes de inyectarse en otros beans —
 * {@code XmlApplicationContext.parseBeans} registra la instancia del {@code <bean>}
 * tal cual). Por eso esta clase implementa {@link DataSource} directamente en vez
 * de exponer {@code getObject()}: así puede inyectarse sin cambios en
 * {@code XmlApplicationContext} como {@code <property name="dataSource" ref="...">}
 * de un {@code JdbcTemplate}.
 *
 * El lookup se hace de forma inmediata y se cachea al asignar {@link #setJndiName},
 * equivalente al comportamiento por defecto de Spring ({@code lookupOnStartup=true,
 * cache=true}) — falla rápido en el arranque si el nombre JNDI no existe.
 *
 * <pre>
 *   {@code <bean id="dataSource" class="com.nimbusframework.jdbc.datasource.JndiObjectFactoryBean">}
 *     {@code <property name="jndiName" value="java:comp/env/jdbc/miDataSource"/>}
 *   {@code </bean>}
 * </pre>
 */
public class JndiObjectFactoryBean extends AbstractDataSource {

    private DataSource target;

    /**
     * Resuelve {@code jndiName} de inmediato vía {@link InitialContext#lookup} y cachea el
     * {@link DataSource} resultante.
     * @throws IllegalStateException si el lookup falla o el objeto resuelto no es un {@code DataSource}.
     */
    public void setJndiName(String jndiName) {
        Object resolved;
        try {
            resolved = new InitialContext().lookup(jndiName);
        } catch (NamingException e) {
            throw new IllegalStateException("No se pudo resolver el JNDI name '" + jndiName + "'", e);
        }
        if (!(resolved instanceof DataSource)) {
            throw new IllegalStateException("El objeto JNDI '" + jndiName + "' no es un javax.sql.DataSource"
                + " (" + (resolved != null ? resolved.getClass().getName() : "null") + ")");
        }
        this.target = (DataSource) resolved;
    }

    /** Delega al {@link DataSource} resuelto por {@link #setJndiName}. */
    @Override
    public Connection getConnection() throws SQLException {
        return target.getConnection();
    }

    /** Delega al {@link DataSource} resuelto por {@link #setJndiName}. */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return target.getConnection(username, password);
    }
}

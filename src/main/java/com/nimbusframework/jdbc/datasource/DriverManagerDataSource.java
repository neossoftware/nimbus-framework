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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * {@link javax.sql.DataSource} que abre una conexión nueva vía {@link DriverManager}
 * en cada llamada a {@link #getConnection()} — sin pool. Pensada para desarrollo,
 * standalone o cargas bajas; en producción con concurrencia real conviene un pool
 * (o, en WAS/Tomcat, el DataSource del propio contenedor vía {@link JndiObjectFactoryBean}).
 *
 * Se configura como cualquier bean XML:
 * <pre>
 *   {@code <bean id="dataSource" class="com.nimbusframework.jdbc.datasource.DriverManagerDataSource">}
 *     {@code <property name="driverClassName" value="${db.driver}"/>}
 *     {@code <property name="url" value="${db.url}"/>}
 *     {@code <property name="username" value="${db.user}"/>}
 *     {@code <property name="password" value="${db.password}"/>}
 *   {@code </bean>}
 * </pre>
 */
public class DriverManagerDataSource extends AbstractDataSource {

    private String url;
    private String username;
    private String password;

    public void setDriverClassName(String driverClassName) {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC no encontrado en el classpath: " + driverClassName, e);
        }
    }

    public void setUrl(String url)           { this.url = url; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}

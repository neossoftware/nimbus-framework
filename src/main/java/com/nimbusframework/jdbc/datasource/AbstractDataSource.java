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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Base para implementaciones simples de {@link DataSource}: resuelve el boilerplate
 * de la interfaz (log writer, login timeout, unwrap) que ninguna de las dos
 * implementaciones de Nimbus ({@link DriverManagerDataSource}, {@link JndiObjectFactoryBean})
 * necesita personalizar. Análogo a {@code org.springframework.jdbc.datasource.AbstractDataSource}.
 */
public abstract class AbstractDataSource implements DataSource {

    private PrintWriter logWriter;
    private int         loginTimeout;

    /** {@inheritDoc} */
    @Override public PrintWriter getLogWriter()                  { return logWriter; }
    /** {@inheritDoc} */
    @Override public void        setLogWriter(PrintWriter out)   { this.logWriter = out; }
    /** {@inheritDoc} */
    @Override public int         getLoginTimeout()                { return loginTimeout; }
    /** {@inheritDoc} */
    @Override public void        setLoginTimeout(int seconds)     { this.loginTimeout = seconds; }

    /** No soportado — siempre lanza {@link SQLFeatureNotSupportedException}. */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("java.util.logging no soportado");
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) return (T) this;
        throw new SQLException(getClass().getName() + " no envuelve " + iface.getName());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }
}

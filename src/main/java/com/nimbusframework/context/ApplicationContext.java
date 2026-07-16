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
package com.nimbusframework.context;

import com.nimbusframework.view.ViewResolver;
import com.nimbusframework.web.HandlerInterceptor;

import java.util.Collection;
import java.util.List;

/**
 * Contrato del contenedor de beans del framework.
 */
public interface ApplicationContext {
    Object getBean(String name);
    <T> T getBean(String name, Class<T> type);
    <T> T getBean(Class<T> type);
    Collection<Object> getAllBeans();
    ViewResolver getViewResolver();

    /** Retorna el valor de una propiedad cargada con {@code <properties file="..."/>}, o null. */
    String getProperty(String key);

    /** Retorna el valor de una propiedad, o {@code defaultValue} si no existe. */
    String getProperty(String key, String defaultValue);

    /** Retorna los interceptores registrados en {@code <interceptors>}, en orden de registro. */
    List<HandlerInterceptor> getInterceptors();
}

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
package com.nimbusframework.annotation;

import java.lang.annotation.*;

/**
 * Vincula un parámetro del método al parámetro HTTP del request con el nombre indicado.
 *
 * Ejemplos:
 * <pre>
 *   // requerido
 *   public String buscar(@RequestParam("query") String query, ...)
 *
 *   // opcional con valor por defecto
 *   public String listar(@RequestParam(value="page", defaultValue="1") int page, ...)
 *
 *   // no requerido, puede llegar null
 *   public String filtrar(@RequestParam(value="filtro", required=false) String filtro, ...)
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestParam {

    String value();

    boolean required() default true;

    /** Valor usado cuando el parámetro no viene en el request. */
    String defaultValue() default "";
}

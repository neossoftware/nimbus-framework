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
 * Vincula un segmento de la URL al parámetro del método.
 *
 * Ejemplos:
 * <pre>
 *   // template: /usuario/{id}.do   →   URL: /usuario/USR-001.do
 *   {@code public ModelAndView show(@PathVariable("id") String id)}
 *
 *   // template: /curso/{cursoId}/alumno/{alumnoId}.do
 *   {@code public String detalle(@PathVariable("cursoId") long cursoId,}
 *   {@code                       @PathVariable("alumnoId") long alumnoId)}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface PathVariable {
    /** Nombre de la variable de plantilla en la URL (p.ej. "id" en "/usuario/{id}"). */
    String value();
}

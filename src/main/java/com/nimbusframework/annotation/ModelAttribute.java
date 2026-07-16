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
 * En parámetros: indica que el framework debe hacer binding automático
 * de los request parameters hacia los campos del bean (por nombre de campo).
 *
 * En métodos (uso futuro): marca un método cuyo retorno se agrega al Model
 * antes de ejecutar cualquier handler del controlador.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Documented
public @interface ModelAttribute {
    /** Nombre con el que el bean se expone en el Model (opcional). */
    String value() default "";
}

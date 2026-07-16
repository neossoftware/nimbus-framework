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
 * Marca un método como manejador de excepciones.
 *
 * Puede usarse dentro de un @Controller / @RestController (alcance local)
 * o dentro de un @ControllerAdvice (alcance global).
 *
 * Los tipos de excepción se pueden declarar en value() o inferir del
 * primer parámetro de tipo Throwable del método.
 *
 * Firmas válidas:
 *   ResponseEntity<MyError> handleAll(Exception ex)
 *   ResponseEntity<MyError> handleAll(Exception ex, HttpServletRequest req)
 *   String                  handleMvc(IllegalArgumentException ex, Model model)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ExceptionHandler {
    /** Tipos de excepción que maneja este método. Si vacío, se infiere del parámetro. */
    Class<? extends Throwable>[] value() default {};
}

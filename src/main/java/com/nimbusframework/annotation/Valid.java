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
 * Marca un parámetro @RequestBody o @ModelAttribute para ser validado con las
 * anotaciones JSR-303 del framework (NotNull, NotBlank, Size, Min, Max, Email).
 *
 * Si el parámetro siguiente en la firma del método es un
 * {@link com.nimbusframework.validation.BindingResult}, los errores se acumulan ahí
 * en vez de lanzar una excepción. Si no hay un BindingResult siguiente y la
 * validación falla, se lanza {@link com.nimbusframework.validation.ValidationException}
 * que puede ser capturada con @ExceptionHandler en un @ControllerAdvice.
 *
 * Equivalente a {@link Validated}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface Valid {}

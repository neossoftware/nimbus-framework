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
 * Marca un método del controller que se invoca ANTES de bindear/validar cada
 * {@code @ModelAttribute}/{@code @RequestBody} de ese controller, recibiendo un
 * {@link com.nimbusframework.web.WebDataBinder} para personalizarlo (típicamente,
 * conectar un {@link com.nimbusframework.validation.Validator} custom vía
 * {@code binder.setValidator(...)}).
 *
 * El método debe tener exactamente un parámetro de tipo WebDataBinder; puede
 * ser private/protected/public.
 *
 * <pre>
 *   {@code @InitBinder}
 *   protected void initBinder(WebDataBinder binder) {
 *       binder.setValidator(this.validator);
 *   }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface InitBinder {
}

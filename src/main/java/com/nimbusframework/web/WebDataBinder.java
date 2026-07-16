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
package com.nimbusframework.web;

import com.nimbusframework.validation.Validator;

/**
 * Se pasa a los métodos {@code @InitBinder} del controller antes de validar un
 * {@code @ModelAttribute}/{@code @RequestBody}, para que puedan personalizar
 * cómo se valida ese bean (típicamente, conectar un {@link Validator} custom).
 *
 * <pre>
 *   {@code @InitBinder}
 *   protected void initBinder(WebDataBinder binder) {
 *       binder.setValidator(this.validator);
 *   }
 * </pre>
 */
public class WebDataBinder {

    private Validator validator;

    public void setValidator(Validator validator) { this.validator = validator; }
    public Validator getValidator()                { return validator; }
}

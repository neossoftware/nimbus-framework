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
 * Dispara la validación del framework (NotNull, NotBlank, Size, Min, Max, Email)
 * sobre el parámetro anotado, igual que {@link Valid}.
 *
 * Se usa típicamente en parámetros @ModelAttribute de controladores MVC:
 * <pre>
 *   public String search(@ModelAttribute("searchbean") @Validated SearchBean bean,
 *                         BindingResult result, Model model) {
 *       if (result.hasErrors()) {
 *           model.addAttribute("formfail", "true");
 *       } else {
 *           // process...
 *       }
 *       return "myview";
 *   }
 * </pre>
 *
 * Si el parámetro siguiente en la firma es un
 * {@link com.nimbusframework.validation.BindingResult}, los errores se acumulan ahí
 * y el controlador decide qué hacer. Si no hay un BindingResult siguiente y
 * la validación falla, se lanza {@link com.nimbusframework.validation.ValidationException}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface Validated {}

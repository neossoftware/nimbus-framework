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
package com.nimbusframework.validation;

/**
 * Validación custom en código Java (alternativa a las anotaciones @NotBlank/@Size/etc.).
 * Se registra como bean XML y se conecta al binding de un controller vía
 * {@code @InitBinder} + {@link com.nimbusframework.web.WebDataBinder#setValidator(Validator)}.
 *
 * <pre>
 *   public class CursoValidator implements Validator {
 *       {@code @Override}
 *       public boolean supports(Class&lt;?&gt; clazz) { return Curso.class.isAssignableFrom(clazz); }
 *
 *       {@code @Override}
 *       public void validate(Object target, Errors errors) {
 *           Curso curso = (Curso) target;
 *           if (curso.getNombre() == null || curso.getNombre().trim().isEmpty()) {
 *               errors.rejectValue("nombre", "curso.nombre.obligatorio");
 *           }
 *           errors.reject("curso.regla.global");
 *       }
 *   }
 * </pre>
 */
public interface Validator {

    /** @return true si este validador sabe validar instancias de {@code clazz}. */
    boolean supports(Class<?> clazz);

    void validate(Object target, Errors errors);
}

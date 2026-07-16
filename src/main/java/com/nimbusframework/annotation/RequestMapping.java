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

import com.nimbusframework.web.RequestMethod;

import java.lang.annotation.*;

/**
 * Mapea una URL a una clase controlador (prefijo) o a un método (ruta completa).
 *
 * Uso en clase (prefijo):
 *   @RequestMapping("/api/cursos")
 *   public class CursoRestController { ... }
 *
 * Uso en método:
 *   @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
 *   public ResponseEntity<Curso> buscar(@PathVariable("id") int id) { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface RequestMapping {
    String value() default "";
    RequestMethod[] method() default {};
    String produces() default "";
    String consumes() default "";
}

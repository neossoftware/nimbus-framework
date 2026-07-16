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
 * Marca un campo o un constructor para inyección de dependencias por el
 * ApplicationContext. El contexto resuelve cada dependencia por tipo
 * (type-matching), o por nombre de bean si el campo/parámetro además
 * lleva {@link Qualifier}.
 *
 * En un constructor solo se permite UN {@code @Autowired} por clase — si hay
 * más de uno, el contexto falla al arrancar. Si la clase no declara ningún
 * constructor {@code @Autowired}, se sigue usando el constructor sin
 * argumentos (comportamiento previo, sin cambios).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Documented
public @interface Autowired {
}

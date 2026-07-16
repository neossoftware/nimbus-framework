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
 * Inyecta el valor de una propiedad del archivo .properties en un campo.
 *
 * Sintaxis:
 *   @Value("${clave}")            — falla si la clave no existe (warning en log)
 *   @Value("${clave:default}")    — usa "default" si la clave no existe
 *
 * Tipos soportados: String, int, long, boolean, double.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Value {
    /** Expresión de la propiedad, p.ej. "${app.name}" o "${timeout:30}". */
    String value();
}

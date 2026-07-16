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

/** Representa una violación de constraint en un campo específico. */
public class ConstraintViolation {

    private final String field;
    private final String message;

    /** Crea una violación para el campo {@code field} con el mensaje {@code message}. */
    public ConstraintViolation(String field, String message) {
        this.field   = field;
        this.message = message;
    }

    /** @return el campo afectado. */
    public String getField()   { return field; }
    /** @return el mensaje de error. */
    public String getMessage() { return message; }

    /** @return {@code campo: mensaje}. */
    @Override
    public String toString() {
        return field + ": " + message;
    }
}

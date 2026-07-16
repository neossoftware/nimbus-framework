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

/** Error atado a un campo específico del bean (ver {@link Errors#rejectValue}). */
public class FieldError extends ObjectError {

    private final String field;

    /** Crea un error de validación atado al campo {@code field} del objeto {@code objectName}. */
    public FieldError(String objectName, String field, String code, String message) {
        super(objectName, code, message);
        this.field = field;
    }

    /** @return el nombre del campo al que está atado este error. */
    public String getField() { return field; }

    /** @return {@code campo: mensaje}. */
    @Override
    public String toString() {
        return field + ": " + getMessage();
    }
}

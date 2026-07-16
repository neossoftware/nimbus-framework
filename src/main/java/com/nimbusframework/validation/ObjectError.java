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

/** Error global, no atado a un campo específico (ver {@link Errors#reject}). */
public class ObjectError {

    private final String objectName;
    private final String code;
    private final String message;

    /** Crea un error global para {@code objectName} con código {@code code} y mensaje ya resuelto. */
    public ObjectError(String objectName, String code, String message) {
        this.objectName = objectName;
        this.code       = code;
        this.message    = message;
    }

    /** @return el nombre del objeto validado. */
    public String getObjectName() { return objectName; }
    /** @return el código de mensaje usado para resolver este error. */
    public String getCode()       { return code; }
    /** @return el mensaje ya resuelto. */
    public String getMessage()    { return message; }

    /** @return el mensaje del error. */
    @Override
    public String toString() {
        return message;
    }
}

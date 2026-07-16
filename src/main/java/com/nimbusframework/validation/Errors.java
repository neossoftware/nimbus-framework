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

import java.util.List;

/**
 * Acumula errores de validación de un objeto, con soporte de mensajes
 * resueltos desde un {@link MessageSource} por código. Análogo a
 * {@code org.springframework.validation.Errors}.
 *
 * Implementada por {@link BindingResult}, que es lo que recibe un
 * {@link Validator#validate(Object, Errors)} para reportar los problemas
 * encontrados.
 */
public interface Errors {

    /** @return el nombre del objeto que se está validando. */
    String getObjectName();

    /** Error global (no atado a un campo), usando {@code errorCode} como clave de mensaje. */
    void reject(String errorCode);

    /** Igual, con mensaje por defecto si {@code errorCode} no se encuentra en el MessageSource. */
    void reject(String errorCode, String defaultMessage);

    /** Error atado a {@code field}, usando {@code errorCode} como clave de mensaje. */
    void rejectValue(String field, String errorCode);

    /** Igual, con mensaje por defecto si {@code errorCode} no se encuentra en el MessageSource. */
    void rejectValue(String field, String errorCode, String defaultMessage);

    /** @return true si hay al menos un error, de campo o global. */
    boolean hasErrors();
    /** @return true si hay al menos un error atado a un campo. */
    boolean hasFieldErrors();
    /** @return true si hay al menos un error global. */
    boolean hasGlobalErrors();
    /** @return el total de errores (de campo + globales). */
    int     getErrorCount();

    /** @return todos los errores, de campo y globales. */
    List<ObjectError> getAllErrors();
    /** @return solo los errores atados a un campo. */
    List<FieldError>  getFieldErrors();
    /** @return solo los errores globales. */
    List<ObjectError> getGlobalErrors();
}

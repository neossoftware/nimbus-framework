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
 * Se lanza cuando {@code @Valid}/{@code @Validated} detecta errores en un {@code @RequestBody} o
 * {@code @ModelAttribute} que NO va seguido de un parámetro BindingResult en la firma
 * del método (si va seguido de uno, los errores se entregan ahí en vez de
 * lanzar esta excepción).
 *
 * Capturar con {@code @ExceptionHandler(ValidationException.class)} en un {@code @ControllerAdvice}.
 */
public class ValidationException extends RuntimeException {

    private final BindingResult bindingResult;

    /** @param bindingResult el resultado con los errores de validación encontrados. */
    public ValidationException(BindingResult bindingResult) {
        super("Validación fallida: " + bindingResult.getErrorCount() + " error(es) — " + bindingResult.getAllErrors());
        this.bindingResult = bindingResult;
    }

    /** @return el {@link BindingResult} con los errores que causaron esta excepción. */
    public BindingResult getBindingResult() { return bindingResult; }
}

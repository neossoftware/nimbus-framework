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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación de {@link Errors} que recibe un controller como parámetro,
 * declarado inmediatamente después del {@code @ModelAttribute}/{@code @RequestBody}
 * que valida:
 *
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
 * Si hay un {@link Validator} custom conectado vía {@code @InitBinder}, este es
 * el objeto {@link Errors} que recibe {@link Validator#validate(Object, Errors)}.
 * Si no, se puebla a partir de las anotaciones (@NotBlank/@Size/etc.) vía
 * {@link BeanValidator}.
 *
 * Los códigos pasados a {@link #reject}/{@link #rejectValue} se resuelven contra
 * el {@link MessageSource} del bean "messageSource" del XML, si existe; si no
 * hay MessageSource o el código no está en el bundle, se usa el defaultMessage
 * (o el código mismo, si tampoco hay defaultMessage).
 */
public class BindingResult implements Errors {

    private final String        objectName;
    private final MessageSource messageSource; // nullable

    private final List<ObjectError> allErrors = new ArrayList<>();

    public BindingResult(String objectName, MessageSource messageSource) {
        this.objectName    = objectName;
        this.messageSource = messageSource;
    }

    @Override public String getObjectName() { return objectName; }

    @Override
    public void reject(String errorCode) {
        reject(errorCode, null);
    }

    @Override
    public void reject(String errorCode, String defaultMessage) {
        allErrors.add(new ObjectError(objectName, errorCode, resolveMessage(errorCode, defaultMessage)));
    }

    @Override
    public void rejectValue(String field, String errorCode) {
        rejectValue(field, errorCode, null);
    }

    @Override
    public void rejectValue(String field, String errorCode, String defaultMessage) {
        allErrors.add(new FieldError(objectName, field, errorCode, resolveMessage(errorCode, defaultMessage)));
    }

    private String resolveMessage(String errorCode, String defaultMessage) {
        if (messageSource != null && errorCode != null) {
            String resolved = messageSource.getMessage(errorCode, null, null);
            if (resolved != null) return resolved;
        }
        return (defaultMessage != null) ? defaultMessage : errorCode;
    }

    @Override public boolean hasErrors()      { return !allErrors.isEmpty(); }
    @Override public int     getErrorCount()  { return allErrors.size(); }

    @Override
    public boolean hasFieldErrors() {
        return allErrors.stream().anyMatch(e -> e instanceof FieldError);
    }

    @Override
    public boolean hasGlobalErrors() {
        return allErrors.stream().anyMatch(e -> !(e instanceof FieldError));
    }

    @Override
    public List<ObjectError> getAllErrors() {
        return Collections.unmodifiableList(allErrors);
    }

    @Override
    public List<FieldError> getFieldErrors() {
        List<FieldError> result = new ArrayList<>();
        for (ObjectError e : allErrors) if (e instanceof FieldError) result.add((FieldError) e);
        return result;
    }

    @Override
    public List<ObjectError> getGlobalErrors() {
        List<ObjectError> result = new ArrayList<>();
        for (ObjectError e : allErrors) if (!(e instanceof FieldError)) result.add(e);
        return result;
    }

    @Override
    public String toString() {
        return "BindingResult{objectName=" + objectName + ", errors=" + allErrors + "}";
    }
}

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

import com.nimbusframework.annotation.Email;
import com.nimbusframework.annotation.Max;
import com.nimbusframework.annotation.Min;
import com.nimbusframework.annotation.NotBlank;
import com.nimbusframework.annotation.NotNull;
import com.nimbusframework.annotation.Size;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Valida un bean Java comprobando las anotaciones de constraint del framework
 * (NotNull, NotBlank, Size, Min, Max, Email) en todos los campos declarados
 * incluyendo los heredados.
 */
public final class BeanValidator {

    private static final Logger  log           = Logger.getLogger(BeanValidator.class.getName());
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private BeanValidator() {}

    /** Valida el bean y retorna el resultado (puede tener 0 o más violaciones). */
    public static ValidationResult validate(Object bean) {
        ValidationResult result = new ValidationResult();
        if (bean == null) {
            result.addViolation("(objeto)", "el cuerpo de la petición no puede ser null");
            return result;
        }
        for (Field field : getAllFields(bean.getClass())) {
            try {
                validateField(bean, field, result);
            } catch (IllegalAccessException e) {
                log.warning("No se pudo acceder al campo: " + field.getName() + " — " + e.getMessage());
            }
        }
        return result;
    }

    private static void validateField(Object bean, Field field, ValidationResult result)
            throws IllegalAccessException {

        field.setAccessible(true);
        Object value     = field.get(bean);
        String fieldName = field.getName();

        // @NotNull — no aplica a primitivos (nunca son null tras auto-boxing)
        NotNull notNull = field.getAnnotation(NotNull.class);
        if (notNull != null && !field.getType().isPrimitive() && value == null) {
            result.addViolation(fieldName, notNull.message());
        }

        // @NotBlank — solo para String
        NotBlank notBlank = field.getAnnotation(NotBlank.class);
        if (notBlank != null) {
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                result.addViolation(fieldName, notBlank.message());
            }
        }

        // @Size — String, Collection, arrays
        Size size = field.getAnnotation(Size.class);
        if (size != null && value != null) {
            int len = measureLength(value);
            if (len < size.min() || len > size.max()) {
                result.addViolation(fieldName, size.message()
                    + " (mín=" + size.min() + ", máx=" + size.max() + ", actual=" + len + ")");
            }
        }

        // @Min — tipos numéricos
        Min min = field.getAnnotation(Min.class);
        if (min != null && value != null) {
            long numVal = toLong(value);
            if (numVal < min.value()) {
                result.addViolation(fieldName, min.message()
                    + " (mínimo=" + min.value() + ", actual=" + numVal + ")");
            }
        }

        // @Max — tipos numéricos
        Max max = field.getAnnotation(Max.class);
        if (max != null && value != null) {
            long numVal = toLong(value);
            if (numVal > max.value()) {
                result.addViolation(fieldName, max.message()
                    + " (máximo=" + max.value() + ", actual=" + numVal + ")");
            }
        }

        // @Email — solo String
        Email email = field.getAnnotation(Email.class);
        if (email != null) {
            if (value == null || !(value instanceof String)
                    || !EMAIL_PATTERN.matcher((String) value).matches()) {
                result.addViolation(fieldName, email.message()
                    + " (valor: " + value + ")");
            }
        }
    }

    private static int measureLength(Object value) {
        if (value instanceof String)    return ((String) value).length();
        if (value instanceof Collection) return ((Collection<?>) value).size();
        if (value.getClass().isArray()) return java.lang.reflect.Array.getLength(value);
        return 0;
    }

    private static long toLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}

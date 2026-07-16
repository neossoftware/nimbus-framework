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
package com.nimbusframework.bind;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Crea una instancia del tipo indicado y puebla sus campos con los
 * parámetros del request que coincidan por nombre de campo.
 *
 * Tipos soportados para conversión automática:
 *   String, int/Integer, long/Long, double/Double, float/Float,
 *   boolean/Boolean, short/Short
 */
public class ModelAttributeBinder {

    private static final Logger log = Logger.getLogger(ModelAttributeBinder.class.getName());

    public static Object bind(Class<?> type, HttpServletRequest request) throws Exception {
        log.fine("Binding @ModelAttribute -> " + type.getSimpleName());
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Field field : getAllFields(type)) {
            String paramValue = request.getParameter(field.getName());
            if (paramValue != null && !paramValue.isEmpty()) {
                field.setAccessible(true);
                field.set(instance, convert(paramValue, field.getType()));
                log.fine("  " + field.getName() + " = \"" + paramValue + "\"");
            }
        }
        return instance;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Object convert(String value, Class<?> target) {
        return TypeConverter.convert(value, target);
    }
}

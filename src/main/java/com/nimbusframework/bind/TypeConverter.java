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

/**
 * Convierte un String (valor crudo del request) al tipo Java destino.
 * Usado por ModelAttributeBinder y por la resolución de @RequestParam.
 */
public class TypeConverter {

    public static Object convert(String value, Class<?> target) {
        if (value == null)                                      return null;
        if (target == String.class)                             return value;
        if (target == int.class     || target == Integer.class) return Integer.parseInt(value.trim());
        if (target == long.class    || target == Long.class)    return Long.parseLong(value.trim());
        if (target == double.class  || target == Double.class)  return Double.parseDouble(value.trim());
        if (target == float.class   || target == Float.class)   return Float.parseFloat(value.trim());
        if (target == short.class   || target == Short.class)   return Short.parseShort(value.trim());
        if (target == boolean.class || target == Boolean.class) return Boolean.parseBoolean(value.trim());
        return value;
    }
}

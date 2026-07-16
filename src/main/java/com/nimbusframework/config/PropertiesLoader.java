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
package com.nimbusframework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Carga archivos .properties desde el classpath o el sistema de archivos.
 *
 * Formatos soportados en {@code location}:
 *   "classpath:app.properties"   → busca en el ClassLoader del contexto
 *   "file:/ruta/absoluta/app.properties" → lee del sistema de archivos
 *   "app.properties"             → intenta classpath primero
 */
public final class PropertiesLoader {

    private static final Logger log = Logger.getLogger(PropertiesLoader.class.getName());

    private PropertiesLoader() {}

    /**
     * Carga las propiedades desde {@code location}, según el prefijo (ver formatos soportados arriba).
     *
     * @param classLoader usado para la búsqueda en el classpath.
     * @throws IOException si el archivo no existe o no puede leerse.
     */
    public static Properties load(String location, ClassLoader classLoader) throws IOException {
        String path = location;

        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        } else if (path.startsWith("file:")) {
            path = path.substring("file:".length());
            return loadFromFile(path);
        }

        // Classpath lookup
        InputStream is = classLoader.getResourceAsStream(path);
        if (is == null) {
            // Fallback: leading slash
            is = classLoader.getResourceAsStream("/" + path);
        }
        if (is == null) {
            throw new IOException("Archivo de propiedades no encontrado en classpath: " + path);
        }
        Properties props = new Properties();
        try (InputStream stream = is) {
            props.load(stream);
        }
        log.info("Propiedades cargadas desde classpath: " + path + " (" + props.size() + " entradas)");
        return props;
    }

    private static Properties loadFromFile(String filePath) throws IOException {
        Properties props = new Properties();
        try (InputStream is = new java.io.FileInputStream(filePath)) {
            props.load(is);
        }
        log.info("Propiedades cargadas desde archivo: " + filePath + " (" + props.size() + " entradas)");
        return props;
    }
}

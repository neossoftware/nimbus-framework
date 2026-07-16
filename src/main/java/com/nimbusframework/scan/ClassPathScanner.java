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
package com.nimbusframework.scan;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Escanea un paquete base buscando clases en el classpath.
 * Compatible con despliegues WAR explodidos en WAS 8.5 (clases en WEB-INF/classes).
 */
public class ClassPathScanner {

    private static final Logger log = Logger.getLogger(ClassPathScanner.class.getName());

    public static List<Class<?>> scan(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace('.', '/');
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resource = loader.getResource(path);
        if (resource == null) {
            log.warning("Paquete no encontrado en classpath: " + basePackage);
            return classes;
        }
        try {
            File directory = new File(resource.toURI());
            findClasses(directory, basePackage, classes);
            log.info("Escaneo de '" + basePackage + "' completado: " + classes.size() + " clase(s) encontrada(s)");
        } catch (Exception e) {
            throw new RuntimeException("Error escaneando paquete: " + basePackage, e);
        }
        return classes;
    }

    private static void findClasses(File directory, String packageName, List<Class<?>> result) {
        if (!directory.exists() || !directory.isDirectory()) return;
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName(), result);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    result.add(Class.forName(className));
                    log.fine("Clase cargada: " + className);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    log.fine("Clase omitida (no cargable): " + className);
                }
            }
        }
    }
}

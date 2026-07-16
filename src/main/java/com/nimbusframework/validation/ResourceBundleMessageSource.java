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

import com.nimbusframework.config.PropertiesLoader;

import java.text.MessageFormat;
import java.util.Properties;

/**
 * {@link MessageSource} respaldado por un único .properties, resuelto desde el
 * classpath (o filesystem) con la misma convención que {@code <properties file="...">}
 * ("classpath:", "file:", o classpath por defecto). Se declara como bean XML:
 *
 * <pre>
 *   {@code <bean id="messageSource" class="com.nimbusframework.validation.ResourceBundleMessageSource">}
 *     {@code <property name="basename"><value>classpath:application/localization/trust_resource</value></property>}
 *   {@code </bean>}
 * </pre>
 *
 * El basename NO lleva extensión — se le agrega ".properties" automáticamente.
 * Los mensajes soportan placeholders {0}, {1}... (java.text.MessageFormat).
 *
 * Nota: a diferencia de Spring's ReloadableResourceBundleMessageSource, esta
 * implementación carga el archivo una vez (primer uso) y lo cachea — no
 * detecta cambios en caliente.
 */
public class ResourceBundleMessageSource implements MessageSource {

    private volatile String     basename;
    private volatile Properties messages;

    /** Setter para {@code <property name="basename" value="..."/>} (JavaBean). */
    public void setBasename(String basename) {
        this.basename = basename;
        this.messages = null; // fuerza recarga si se reconfigura
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage) {
        String pattern = loadMessages().getProperty(code);
        return (pattern != null) ? format(pattern, args) : defaultMessage;
    }

    @Override
    public String getMessage(String code, Object[] args) {
        String pattern = loadMessages().getProperty(code);
        if (pattern == null) {
            throw new NoSuchMessageException(code);
        }
        return format(pattern, args);
    }

    private static String format(String pattern, Object[] args) {
        return (args == null || args.length == 0) ? pattern : MessageFormat.format(pattern, args);
    }

    private Properties loadMessages() {
        Properties loaded = messages;
        if (loaded != null) return loaded;
        synchronized (this) {
            if (messages == null) {
                if (basename == null || basename.isEmpty()) {
                    throw new IllegalStateException(
                        "ResourceBundleMessageSource sin 'basename' configurado (<property name=\"basename\" .../>)");
                }
                try {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    messages = PropertiesLoader.load(basename + ".properties", cl);
                } catch (Exception e) {
                    throw new RuntimeException("No se pudo cargar el bundle de mensajes: " + basename, e);
                }
            }
            return messages;
        }
    }
}

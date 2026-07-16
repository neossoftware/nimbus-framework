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
package com.nimbusframework.annotation;

import java.lang.annotation.*;

/**
 * Define el scope de un bean administrado por el contenedor.
 *
 * Valores soportados (usar las constantes de {@link BeanScope}):
 *   "singleton"  — (default) una única instancia por contenedor
 *   "prototype"  — nueva instancia en cada inyección / getBean()
 *
 * Uso:
 * <pre>
 *   {@code @Component}
 *   {@code @Scope(BeanScope.PROTOTYPE)}
 *   {@code public class MiServicio { ... }}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Scope {
    /** Scope del bean, ver constantes de {@link BeanScope}. */
    String value() default BeanScope.SINGLETON;
}

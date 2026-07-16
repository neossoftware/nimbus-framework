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
 * Desambigua qué bean inyectar cuando hay más de una implementación
 * candidata para el mismo tipo. El valor es el nombre de bean (el mismo
 * que usa el component-scan: nombre de clase con la inicial en minúscula,
 * o el "id" declarado en un <bean> XML).
 *
 * Válida en campos y en parámetros de constructor:
 * <pre>
 *   {@code @Autowired}
 *   {@code @Qualifier("mockPaymentService")}
 *   private PaymentService paymentService;
 *
 *   {@code @Autowired}
 *   public OrderService({@code @Qualifier("mockPaymentService")} PaymentService svc) {
 *       this.paymentService = svc;
 *   }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface Qualifier {
    String value();
}

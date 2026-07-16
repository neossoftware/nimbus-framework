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
package com.nimbusframework.web;

/**
 * Envuelve la respuesta HTTP: cuerpo, cabeceras y código de estado.
 * API compatible con Spring's ResponseEntity para facilitar la migración.
 *
 * Uso típico:
 *   return ResponseEntity.ok(lista);
 *   return new ResponseEntity<>(dto, HttpStatus.CREATED);
 *   return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
 */
public class ResponseEntity<T> {

    private final T           body;
    private final HttpStatus  status;
    private final HttpHeaders headers;

    public ResponseEntity(T body, HttpStatus status) {
        this(body, new HttpHeaders(), status);
    }

    public ResponseEntity(T body, HttpHeaders headers, HttpStatus status) {
        this.body    = body;
        this.headers = (headers != null) ? headers : new HttpHeaders();
        this.status  = status;
    }

    public T           getBody()    { return body; }
    public HttpStatus  getStatus()  { return status; }
    public HttpHeaders getHeaders() { return headers; }

    // ------------------------------------------------------------------
    // Static factories
    // ------------------------------------------------------------------

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    public static ResponseEntity<Void> noContent() {
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    public static ResponseEntity<Void> notFound() {
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    public static BodyBuilder status(HttpStatus status) {
        return new BodyBuilder(status);
    }

    public static BodyBuilder ok() {
        return status(HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------

    public static class BodyBuilder {

        private final HttpStatus  status;
        private final HttpHeaders headers = new HttpHeaders();

        BodyBuilder(HttpStatus status) {
            this.status = status;
        }

        public BodyBuilder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        public BodyBuilder contentType(String contentType) {
            headers.setContentType(contentType);
            return this;
        }

        public <T> ResponseEntity<T> body(T body) {
            return new ResponseEntity<>(body, headers, status);
        }

        public ResponseEntity<Void> build() {
            return new ResponseEntity<>(null, headers, status);
        }
    }
}

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
 * <pre>
 *   {@code return ResponseEntity.ok(lista);}
 *   {@code return new ResponseEntity<>(dto, HttpStatus.CREATED);}
 *   {@code return ResponseEntity.status(HttpStatus.NOT_FOUND).build();}
 * </pre>
 */
public class ResponseEntity<T> {

    private final T           body;
    private final HttpStatus  status;
    private final HttpHeaders headers;

    /** Crea una respuesta con cuerpo y estado, sin cabeceras adicionales. */
    public ResponseEntity(T body, HttpStatus status) {
        this(body, new HttpHeaders(), status);
    }

    /** Crea una respuesta con cuerpo, cabeceras y estado. */
    public ResponseEntity(T body, HttpHeaders headers, HttpStatus status) {
        this.body    = body;
        this.headers = (headers != null) ? headers : new HttpHeaders();
        this.status  = status;
    }

    /** @return el cuerpo de la respuesta. */
    public T           getBody()    { return body; }
    /** @return el código de estado HTTP. */
    public HttpStatus  getStatus()  { return status; }
    /** @return las cabeceras de la respuesta. */
    public HttpHeaders getHeaders() { return headers; }

    // ------------------------------------------------------------------
    // Static factories
    // ------------------------------------------------------------------

    /** Atajo para una respuesta 200 OK con cuerpo. */
    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    /** Atajo para una respuesta 201 Created con cuerpo. */
    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    /** Atajo para una respuesta 204 No Content, sin cuerpo. */
    public static ResponseEntity<Void> noContent() {
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    /** Atajo para una respuesta 404 Not Found, sin cuerpo. */
    public static ResponseEntity<Void> notFound() {
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    /** @return un {@link BodyBuilder} para construir una respuesta con el estado dado. */
    public static BodyBuilder status(HttpStatus status) {
        return new BodyBuilder(status);
    }

    /** @return un {@link BodyBuilder} para una respuesta 200 OK. */
    public static BodyBuilder ok() {
        return status(HttpStatus.OK);
    }

    // ------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------

    /**
     * Builder fluido devuelto por {@link ResponseEntity#status(HttpStatus)} para
     * configurar cabeceras antes de llamar a body() o build().
     */
    public static class BodyBuilder {

        private final HttpStatus  status;
        private final HttpHeaders headers = new HttpHeaders();

        /** @param status el código de estado con el que se construirá la respuesta. */
        BodyBuilder(HttpStatus status) {
            this.status = status;
        }

        /** Agrega una cabecera con el nombre y valor dados. */
        public BodyBuilder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /** Establece la cabecera Content-Type. */
        public BodyBuilder contentType(String contentType) {
            headers.setContentType(contentType);
            return this;
        }

        /** @return la respuesta con el cuerpo, cabeceras y estado configurados. */
        public <T> ResponseEntity<T> body(T body) {
            return new ResponseEntity<>(body, headers, status);
        }

        /** @return la respuesta sin cuerpo, con las cabeceras y estado configurados. */
        public ResponseEntity<Void> build() {
            return new ResponseEntity<>(null, headers, status);
        }
    }
}

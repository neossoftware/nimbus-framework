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
 * Enumera los códigos de estado HTTP que el framework conoce, con su frase de razón.
 * Análogo a {@code org.springframework.http.HttpStatus}.
 */
public enum HttpStatus {

    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),

    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    private final int    value;
    private final String reasonPhrase;

    HttpStatus(int value, String reasonPhrase) {
        this.value        = value;
        this.reasonPhrase = reasonPhrase;
    }

    /** @return el código numérico, ej. 200. */
    public int    value()        { return value; }
    /** @return la frase de razón, ej. "OK". */
    public String getReasonPhrase() { return reasonPhrase; }

    /** @return true si el código está en el rango 2xx (éxito). */
    public boolean is2xxSuccessful()    { return value >= 200 && value < 300; }
    /** @return true si el código está en el rango 4xx (error del cliente). */
    public boolean is4xxClientError()   { return value >= 400 && value < 500; }
    /** @return true si el código está en el rango 5xx (error del servidor). */
    public boolean is5xxServerError()   { return value >= 500 && value < 600; }

    /** @return el código y la frase de razón juntos, ej. "200 OK". */
    @Override
    public String toString() { return value + " " + reasonPhrase; }
}

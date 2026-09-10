package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando se busca un equipo por un id que no existe.
 * Anotada con {@code @ResponseStatus(NOT_FOUND)}: Spring responde 404
 * sin necesidad de que el GlobalExceptionHandler la conozca.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EquipoNoEncontradoException extends RuntimeException {

    public EquipoNoEncontradoException(Long id) {
        super("No existe un equipo con id: " + id);
    }
}

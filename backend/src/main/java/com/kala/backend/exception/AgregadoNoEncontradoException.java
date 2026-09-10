package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AgregadoNoEncontradoException extends RuntimeException {

    public AgregadoNoEncontradoException(Long equipoId) {
        super("No se encontró un agregado para el equipo " + equipoId);
    }
}
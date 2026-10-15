package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CuestionarioNoEncontradoException extends RuntimeException {

    public CuestionarioNoEncontradoException(Long id) {
        super("No existe un cuestionario con id: " + id);
    }
}

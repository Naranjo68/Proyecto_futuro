package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Con @ResponseStatus, si esta excepción sale del controller sin capturar,
// Spring responde 404 automáticamente.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EquipoNoEncontradoException extends RuntimeException {

    public EquipoNoEncontradoException(Long id) {
        super("No existe un equipo con id: " + id);
    }
}

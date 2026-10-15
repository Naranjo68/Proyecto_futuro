package com.kala.backend.exception;

// La traduce a HTTP 404 el GlobalExceptionHandler.
public class EquipoNoEncontradoException extends RuntimeException {

    public EquipoNoEncontradoException(Long id) {
        super("No existe un equipo con id: " + id);
    }
}

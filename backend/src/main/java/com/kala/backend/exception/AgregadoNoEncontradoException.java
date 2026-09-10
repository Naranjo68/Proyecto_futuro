package com.kala.backend.exception;

// La traduce a HTTP 404 el GlobalExceptionHandler.
public class AgregadoNoEncontradoException extends RuntimeException {

    public AgregadoNoEncontradoException(Long equipoId) {
        super("No hay check-ins registrados hoy para el equipo con id: " + equipoId);
    }
}

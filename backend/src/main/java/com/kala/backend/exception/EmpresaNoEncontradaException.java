package com.kala.backend.exception;

// La traduce a HTTP 404 el GlobalExceptionHandler.
public class EmpresaNoEncontradaException extends RuntimeException {

    public EmpresaNoEncontradaException(Long id) {
        super("No existe una empresa con id: " + id);
    }
}

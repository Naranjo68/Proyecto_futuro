package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SemanaInvalidaException extends RuntimeException {
    public SemanaInvalidaException() {
        super("La fecha de la semana debe corresponder a un lunes.");
    }

    public SemanaInvalidaException(String mensaje) {
        super(mensaje);
    }
    
}

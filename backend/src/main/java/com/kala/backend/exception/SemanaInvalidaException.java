package com.kala.backend.exception;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 400: el dashboard consulta una sola ventana semanal y la semana empieza el lunes.
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SemanaInvalidaException extends RuntimeException {

    public SemanaInvalidaException(LocalDate semana) {
        super("La semana debe empezar en lunes y " + semana + " no lo es");
    }
}

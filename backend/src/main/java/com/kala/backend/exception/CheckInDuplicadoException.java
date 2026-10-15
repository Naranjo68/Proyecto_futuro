package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 409: ya hubo un envío en la ventana semanal. El mensaje no identifica a nadie.
@ResponseStatus(HttpStatus.CONFLICT)
public class CheckInDuplicadoException extends RuntimeException {

    public CheckInDuplicadoException() {
        super("Ya respondiste este cuestionario en la ventana actual");
    }
}

package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItemCuestionarioNoEncontradoException extends RuntimeException {

    public ItemCuestionarioNoEncontradoException(Long itemId) {
        super("No existe el ítem de cuestionario " + itemId);
    }
}

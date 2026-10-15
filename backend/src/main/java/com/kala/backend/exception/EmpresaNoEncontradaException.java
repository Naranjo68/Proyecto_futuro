
package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmpresaNoEncontradaException extends RuntimeException {

    public EmpresaNoEncontradaException(Long id) {
        super("No existe una empresa con id: " + id);
    }
}

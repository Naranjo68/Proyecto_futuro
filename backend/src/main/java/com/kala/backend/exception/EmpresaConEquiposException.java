
package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmpresaConEquiposException extends RuntimeException {

    public EmpresaConEquiposException(Long id) {
        super("No se puede eliminar la empresa con id " + id
                + " porque tiene equipos asociados");
    }
}

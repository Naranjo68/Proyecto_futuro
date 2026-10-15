package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PresupuestoExcedidoException extends RuntimeException {

    public PresupuestoExcedidoException(String message) {
        super(message);
    }

    public PresupuestoExcedidoException(Long empresaId, double acumulado, double maximo) {
        super(String.format("La empresa %d excede el presupuesto de privacidad: acumulado %.4f, maximo %.4f",
                empresaId, acumulado, maximo));
    }
}

package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PresupuestoExcedidoException extends RuntimeException {
    public PresupuestoExcedidoException(Long empresaId, Double calculado, Double maximo) {
        super(String.format("El presupuesto de privacidad para la empresa %d fue excedido. Calculado: %s, Máximo: %s", empresaId, calculado, maximo));
    }
}

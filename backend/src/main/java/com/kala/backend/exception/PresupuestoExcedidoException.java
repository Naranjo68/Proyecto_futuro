package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 422: activar el cuestionario haría que una persona gaste más privacidad (ε) al mes que el máximo permitido.
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class PresupuestoExcedidoException extends RuntimeException {

    public PresupuestoExcedidoException(double epsilonMensual, double epsilonMaximo) {
        super(String.format("El ε mensual por persona sería %.2f y el máximo permitido es %.2f",
                epsilonMensual, epsilonMaximo));
    }
}

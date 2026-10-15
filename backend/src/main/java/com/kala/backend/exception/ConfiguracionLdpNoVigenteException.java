package com.kala.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 422: el dispositivo perturbó la respuesta con una configuración que ya no es la vigente del ítem.
// Aceptarla mezclaría datos recolectados con parámetros distintos y sesgaría el estimador.
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class ConfiguracionLdpNoVigenteException extends RuntimeException {

    public ConfiguracionLdpNoVigenteException(Long itemId) {
        super("La configuración enviada no es la vigente del ítem " + itemId);
    }
}

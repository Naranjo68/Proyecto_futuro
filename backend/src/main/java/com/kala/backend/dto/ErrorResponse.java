package com.kala.backend.dto;

import java.time.LocalDateTime;

// Cuerpo JSON uniforme para cualquier error que devuelve la API.
public record ErrorResponse(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta
) {
}

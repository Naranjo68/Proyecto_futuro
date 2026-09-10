package com.kala.backend.dto;

import jakarta.validation.constraints.NotNull;

// Datos que llegan en el cuerpo de POST /api/checkin.
public record CheckInRequest(

        @NotNull
        Long equipoId,

        @NotNull
        Boolean respuestaRuidosa
) {
}

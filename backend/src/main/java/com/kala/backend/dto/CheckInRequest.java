package com.kala.backend.dto;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull Long equipoId,
        @NotNull Boolean respuestaRuidosa
) {
}
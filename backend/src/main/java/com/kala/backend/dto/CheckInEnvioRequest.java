package com.kala.backend.dto;

import jakarta.validation.constraints.NotNull;

// Envío de un check-in desde el dispositivo. La respuesta llega YA perturbada (privacidad diferencial local).
// El equipo y la persona no vienen en el cuerpo: salen del token.
public record CheckInEnvioRequest(
        @NotNull Long itemId,
        @NotNull Long configuracionLdpId,
        @NotNull Boolean respuestaRuidosa
) {
}

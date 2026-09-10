package com.kala.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos que llegan en el cuerpo de POST y PUT.
public record EquipoRequest(

        @NotBlank                 // no null y no solo espacios
        String nombre,

        @NotNull                  // obligatorio
        Long empresaId
) {
}

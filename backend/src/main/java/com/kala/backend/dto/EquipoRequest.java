package com.kala.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para crear o actualizar un {@link com.kala.backend.model.Equipo}.
 */
public record EquipoRequest(

        @NotBlank
        String nombre,

        @NotNull
        Long empresaId
) {
}

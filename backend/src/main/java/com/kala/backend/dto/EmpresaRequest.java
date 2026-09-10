package com.kala.backend.dto;

import jakarta.validation.constraints.NotBlank;

// Datos que llegan en el cuerpo de POST y PUT.
public record EmpresaRequest(

        @NotBlank                 // no null y no solo espacios
        String nombre,

        @NotBlank
        String rubro
) {
}

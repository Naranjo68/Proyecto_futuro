
package com.kala.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmpresaRequest(
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "El RUC debe contener exactamente 11 dígitos")
        String ruc,

        @NotBlank String nombre,
        @NotBlank String rubro
) {
}

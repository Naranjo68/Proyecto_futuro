
package com.kala.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EmpresaConEquiposRequest(
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "El RUC debe contener exactamente 11 dígitos")
        String ruc,

        @NotBlank
        String nombre,

        @NotBlank
        String rubro,

        @NotNull
        @NotEmpty
        List<@NotBlank String> equipos
) {
}

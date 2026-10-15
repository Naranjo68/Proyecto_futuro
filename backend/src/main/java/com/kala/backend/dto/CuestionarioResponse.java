package com.kala.backend.dto;

import com.kala.backend.model.Cuestionario;

// Un cuestionario activo de la empresa, sin sus ítems.
public record CuestionarioResponse(
        Long id,
        String codigo,
        String nombre,
        int cadenciaDias,
        Integer itemsPorEnvio
) {
    public static CuestionarioResponse de(Cuestionario c) {
        return new CuestionarioResponse(c.getId(), c.getCodigo(), c.getNombre(), c.getCadenciaDias(),
                c.getItemsPorEnvio() == null ? null : c.getItemsPorEnvio().intValue());
    }
}

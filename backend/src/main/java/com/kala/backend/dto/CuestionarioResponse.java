package com.kala.backend.dto;

import java.util.List;

public record CuestionarioResponse(
        Long id,
        String codigo,
        String nombre,
        Short cadenciaDias,
        Short itemsPorEnvio,
        Boolean activo,
        List<ItemResponse> items
) {}

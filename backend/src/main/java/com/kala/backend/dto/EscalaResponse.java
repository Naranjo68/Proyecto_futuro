package com.kala.backend.dto;

import java.util.List;

public record EscalaResponse(
        Long id,
        String codigo,
        Short valorMin,
        Short valorMax,
        List<OpcionResponse> opciones
) {}

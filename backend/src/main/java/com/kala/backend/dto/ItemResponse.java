package com.kala.backend.dto;

import java.math.BigDecimal;

public record ItemResponse(
        Long id,
        Short orden,
        String tema,
        String texto,
        EscalaResponse escala,
        Long configuracionLdpId,
        String mecanismo,
        BigDecimal probabilidadVerdad
) {}

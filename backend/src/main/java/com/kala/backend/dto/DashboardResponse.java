package com.kala.backend.dto;

public record DashboardResponse(
        Long equipoId,
        int respuestasSi,
        int totalRespuestas
) {
}
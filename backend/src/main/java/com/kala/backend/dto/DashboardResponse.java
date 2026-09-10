package com.kala.backend.dto;

// Lo que devuelve GET /api/dashboard/{equipoId}: el agregado del día del equipo.
public record DashboardResponse(
        Long equipoId,
        int respuestasSi,
        int totalRespuestas
) {
}

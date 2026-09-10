package com.kala.backend.controller;

import com.kala.backend.dto.DashboardResponse;
import com.kala.backend.model.CheckIn;
import com.kala.backend.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CheckInService checkInService;

    public DashboardController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    // 200 con el agregado del día; 404 si el equipo aún no tiene check-ins hoy
    // (si el equipo no existe, el service lanza la excepción -> 404).
    @GetMapping("/{equipoId}")
    public ResponseEntity<DashboardResponse> obtener(@PathVariable Long equipoId) {
        CheckIn agregado = checkInService.buscarAgregadoDeHoy(equipoId);
        if (agregado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DashboardResponse(
                agregado.getEquipoId(),
                agregado.getRespuestasSi(),
                agregado.getTotalRespuestas()));
    }
}

package com.kala.backend.controller;

import com.kala.backend.dto.DashboardResponse;
import com.kala.backend.exception.AgregadoNoEncontradoException;
import com.kala.backend.model.CheckIn;
import com.kala.backend.service.CheckInService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Traduce las peticiones HTTP a llamadas del service. Sin lógica de negocio ni try/catch.
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CheckInService service;

    public DashboardController(CheckInService service) {
        this.service = service;
    }

    // 200 con el agregado de hoy; 404 si no hay check-ins hoy o el equipo no existe.
    @GetMapping("/{equipoId}")
    public DashboardResponse obtener(@PathVariable Long equipoId) {
        CheckIn agregado = service.buscarAgregadoDeHoy(equipoId);
        if (agregado == null) {
            throw new AgregadoNoEncontradoException(equipoId);
        }
        return new DashboardResponse(
                agregado.getEquipoId(),
                agregado.getRespuestasSi(),
                agregado.getTotalRespuestas());
    }
}

package com.kala.backend.controller;

import com.kala.backend.dto.DashboardResponse;
import com.kala.backend.exception.AgregadoNoEncontradoException;
import com.kala.backend.model.CheckIn;
import com.kala.backend.service.CheckInService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CheckInService checkInService;

    public DashboardController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping("/{equipoId}")
    public DashboardResponse obtenerDashboard(@PathVariable Long equipoId) {

        CheckIn checkIn = checkInService.buscarAgregadoDeHoy(equipoId);

        if (checkIn == null) {
            throw new AgregadoNoEncontradoException(equipoId);
        }

        return new DashboardResponse(
                checkIn.getEquipoId(),
                checkIn.getRespuestasSi(),
                checkIn.getTotalRespuestas()
        );
    }
}
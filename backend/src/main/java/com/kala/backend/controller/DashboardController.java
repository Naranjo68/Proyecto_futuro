package com.kala.backend.controller;

import com.kala.backend.dto.DashboardResponse;
import com.kala.backend.dto.DashboardResponse.ItemDashboard;
import com.kala.backend.service.DashboardService;
import com.kala.backend.service.DashboardService.ResultadoDashboard;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Traduce HTTP a una llamada de service. La empresa y el usuario salen del token, nunca de la petición.
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    // 200 con la estimación de la semana; 400 si la semana no empieza en lunes; 404 si el equipo no es de la empresa.
    @GetMapping("/{equipoId}")
    @PreAuthorize("hasAnyRole('RRHH','COORDINADOR_BIENESTAR')")
    public DashboardResponse obtener(
            @PathVariable Long equipoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate semana,
            @AuthenticationPrincipal Jwt jwt) {

        Long empresaId = claimObligatorio(jwt, "empresaId");
        Long usuarioId = claimObligatorio(jwt, "uid");
        ResultadoDashboard r = service.consultar(usuarioId, empresaId, equipoId, semana);
        return new DashboardResponse(
                r.equipoId(),
                r.semanaInicio(),
                r.resultado(),
                r.totalRespuestas(),
                r.items().stream()
                        .map(i -> new ItemDashboard(i.itemId(), i.tema(), i.proporcionEstimada(), i.margenError95()))
                        .toList());
    }

    private static Long claimObligatorio(Jwt jwt, String nombre) {
        Long valor = jwt.getClaim(nombre);
        if (valor == null) {
            throw new AccessDeniedException("El token no contiene " + nombre);
        }
        return valor;
    }
}

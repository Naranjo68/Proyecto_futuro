package com.kala.backend.controller;

import com.kala.backend.dto.CuestionarioResponse;
import com.kala.backend.service.ActivacionCuestionarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CuestionarioController {

    private final ActivacionCuestionarioService activacionService;

    public CuestionarioController(ActivacionCuestionarioService activacionService) {
        this.activacionService = activacionService;
    }

    @PostMapping("/empresas/{empresaId}/cuestionarios/{cuestionarioId}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activar(
            @PathVariable Long empresaId,
            @PathVariable Long cuestionarioId) {
        activacionService.activar(empresaId, cuestionarioId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/empresas/{empresaId}/cuestionarios")
    @PreAuthorize("hasRole('ADMIN') or ((hasRole('RRHH') or hasRole('COORDINADOR_BIENESTAR')) and #jwt.claims['empresaId'] == #empresaId)")
    public ResponseEntity<List<CuestionarioResponse>> listarPorEmpresa(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/cuestionarios/pendientes")
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<List<CuestionarioResponse>> listarPendientes(
            @AuthenticationPrincipal Jwt jwt) {
        Long empresaId = jwt.getClaim("empresaId");
        return ResponseEntity.ok(Collections.emptyList());
    }
}

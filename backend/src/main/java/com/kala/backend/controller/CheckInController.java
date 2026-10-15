package com.kala.backend.controller;

import com.kala.backend.dto.CheckInEnvioRequest;
import com.kala.backend.service.CheckInRegistroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Traduce las peticiones HTTP a llamadas del service. Sin lógica de negocio ni try/catch.
// La persona y su equipo salen del token (claims uid y equipoId), nunca del cuerpo de la petición.
@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInRegistroService service;

    public CheckInController(CheckInRegistroService service) {
        this.service = service;
    }

    // 202 Accepted: el check-in se aceptó; no se devuelve cuerpo.
    // 409 si ya respondió en la ventana, 422 si la configuración no es la vigente, 404 si el ítem no existe.
    @PostMapping
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<Void> registrar(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody CheckInEnvioRequest request) {
        service.registrar(claim(jwt, "uid"), claim(jwt, "equipoId"), request);
        return ResponseEntity.accepted().build();
    }

    // Sin el claim no se puede identificar el equipo ni la ventana de la persona: se rechaza con 403.
    private static Long claim(Jwt jwt, String nombre) {
        Object valor = jwt == null ? null : jwt.getClaim(nombre);
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        throw new AccessDeniedException("El token no incluye el claim " + nombre);
    }
}

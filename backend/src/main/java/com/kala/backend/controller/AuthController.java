package com.kala.backend.controller;

import com.kala.backend.dto.LoginRequest;
import com.kala.backend.dto.TokenResponse;
import com.kala.backend.security.AutenticacionService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Traduce las peticiones HTTP a llamadas del service. Sin lógica de negocio ni try/catch.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticacionService autenticacion;

    public AuthController(AutenticacionService autenticacion) {
        this.autenticacion = autenticacion;
    }

    // 200 con el token; 401 con credenciales inválidas; 400 si falta usuario o contraseña.
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticacion.login(request);
    }

    // Devuelve lo que dice el token de quien consulta; no vuelve a validar credenciales.
    @GetMapping("/me")
    public Map<String, Object> miPerfil(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("username", jwt.getSubject());
        perfil.put("uid", jwt.getClaim("uid"));
        perfil.put("roles", jwt.getClaim("roles"));
        if (jwt.getClaim("empresaId") != null) {
            perfil.put("empresaId", jwt.getClaim("empresaId"));
        }
        if (jwt.getClaim("equipoId") != null) {
            perfil.put("equipoId", jwt.getClaim("equipoId"));
        }
        return perfil;
    }
}

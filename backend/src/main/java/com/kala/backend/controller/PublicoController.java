package com.kala.backend.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint público: sirve para comprobar que el servidor está arriba sin necesidad de credenciales.
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("estado", "API disponible");
    }
}

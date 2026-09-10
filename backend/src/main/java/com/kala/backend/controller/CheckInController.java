package com.kala.backend.controller;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Traduce las peticiones HTTP a llamadas del service. Sin lógica de negocio ni try/catch.
@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    // 202 Accepted: el check-in se aceptó; no se devuelve cuerpo.
    @PostMapping
    public ResponseEntity<Void> registrar(@Valid @RequestBody CheckInRequest request) {
        service.registrar(request);
        return ResponseEntity.accepted().build();
    }
}

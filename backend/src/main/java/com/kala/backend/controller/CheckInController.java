package com.kala.backend.controller;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    // 202 Accepted: se recibió el check-in; no se devuelve cuerpo.
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void registrar(@Valid @RequestBody CheckInRequest request) {
        service.registrar(request);
    }
}

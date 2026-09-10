package com.kala.backend.controller;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public ResponseEntity<Void> registrar(
            @Valid @RequestBody CheckInRequest request) {

        checkInService.registrar(request);

        return ResponseEntity.accepted().build();
    }
}
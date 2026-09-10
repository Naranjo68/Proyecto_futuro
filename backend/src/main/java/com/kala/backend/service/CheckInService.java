package com.kala.backend.service;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.model.CheckIn;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CheckInService {

    private final Map<String, CheckIn> checkIns = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public void registrar(CheckInRequest request) {

        if (request.equipoId() == null) {
            throw new IllegalArgumentException("equipoId es obligatorio");
        }

        LocalDate fecha = LocalDate.now();

        String clave = request.equipoId() + "|" + fecha;

        CheckIn checkIn = checkIns.computeIfAbsent(
                clave,
                key -> new CheckIn(
                        idGenerator.getAndIncrement(),
                        request.equipoId(),
                        fecha
                )
        );

        checkIn.registrar(request.respuestaRuidosa());
    }

    public CheckIn buscarAgregadoDeHoy(Long equipoId) {

        if (equipoId == null) {
            throw new IllegalArgumentException("equipoId es obligatorio");
        }

        String clave = equipoId + "|" + LocalDate.now();

        return checkIns.get(clave);
    }
}
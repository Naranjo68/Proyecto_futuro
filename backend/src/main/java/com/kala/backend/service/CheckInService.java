package com.kala.backend.service;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.model.CheckIn;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class CheckInService {

    // Un agregado por equipo y por día. La clave combina ambos.
    private final Map<String, CheckIn> agregados = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    // Se usa para comprobar que el equipo exista antes de registrar o consultar.
    private final EquipoService equipoService;

    public CheckInService(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    public void registrar(CheckInRequest request) {
        validarEquipoExistente(request.equipoId());
        String clave = clave(request.equipoId(), LocalDate.now());
        CheckIn agregado = agregados.computeIfAbsent(
                clave,
                k -> new CheckIn(secuencia.incrementAndGet(), request.equipoId(), LocalDate.now()));
        agregado.registrar(request.respuestaRuidosa());
    }

    // Agregado de hoy del equipo, o null si todavía no hubo check-ins hoy.
    public CheckIn buscarAgregadoDeHoy(Long equipoId) {
        validarEquipoExistente(equipoId);
        return agregados.get(clave(equipoId, LocalDate.now()));
    }

    private void validarEquipoExistente(Long equipoId) {
        if (equipoId == null) {
            throw new IllegalArgumentException("equipoId es obligatorio");
        }
        equipoService.buscarPorId(equipoId);   // 404 si el equipo no existe
    }

    private String clave(Long equipoId, LocalDate fecha) {
        return equipoId + "|" + fecha;
    }
}

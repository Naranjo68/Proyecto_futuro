package com.kala.backend.model;

import java.time.LocalDate;

// Agregado diario de check-ins de un equipo: cuenta respuestas, no las guarda.
public class CheckIn {

    private final Long id;
    private final Long equipoId;
    private final LocalDate fecha;
    private int respuestasSi;
    private int totalRespuestas;

    public CheckIn(Long id, Long equipoId, LocalDate fecha) {
        this.id = id;
        this.equipoId = equipoId;
        this.fecha = fecha;
    }

    // Suma una respuesta: siempre al total, y al "sí" solo si respuestaSi es true.
    public void registrar(boolean respuestaSi) {
        totalRespuestas++;
        if (respuestaSi) {
            respuestasSi++;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getEquipoId() {
        return equipoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getRespuestasSi() {
        return respuestasSi;
    }

    public int getTotalRespuestas() {
        return totalRespuestas;
    }
}

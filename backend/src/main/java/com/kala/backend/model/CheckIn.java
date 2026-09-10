package com.kala.backend.model;

import java.time.LocalDate;

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
        this.respuestasSi = 0;
        this.totalRespuestas = 0;
    }

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
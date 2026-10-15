package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Subtipo: persona que responde check-ins. La empresa se deriva del equipo (no se guarda aquí).
// equipoId es un Long, no una relación: los módulos no se importan entre sí.
@Entity
@Table(name = "empleado_equipo")
public class EmpleadoEquipo {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    protected EmpleadoEquipo() {
    }

    public EmpleadoEquipo(Long usuarioId, Long equipoId) {
        this.usuarioId = usuarioId;
        this.equipoId = equipoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getEquipoId() {
        return equipoId;
    }
}

package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Subtipo: operador de la plataforma Kala (rol ADMIN). No pertenece a ninguna empresa.
@Entity
@Table(name = "administrador_plataforma")
public class AdministradorPlataforma {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    protected AdministradorPlataforma() {
    }

    public AdministradorPlataforma(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}

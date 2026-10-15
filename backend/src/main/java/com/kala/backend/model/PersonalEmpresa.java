package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Subtipo: personal de una empresa (RRHH o coordinador de bienestar).
// Una persona puede ser a la vez empleado de un equipo y personal de la empresa.
@Entity
@Table(name = "personal_empresa")
public class PersonalEmpresa {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private RolPersonal rol;

    protected PersonalEmpresa() {
    }

    public PersonalEmpresa(Long usuarioId, Long empresaId, RolPersonal rol) {
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.rol = rol;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public RolPersonal getRol() {
        return rol;
    }
}

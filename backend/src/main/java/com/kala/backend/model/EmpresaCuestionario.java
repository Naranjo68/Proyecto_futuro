package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

// Qué cuestionarios tiene activos una empresa. La empresa se referencia solo por id (la FK llega en V7).
@Entity
@Table(name = "empresa_cuestionario")
public class EmpresaCuestionario {

    @EmbeddedId
    private EmpresaCuestionarioId id;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected EmpresaCuestionario() {
        // Requerido por JPA.
    }

    public EmpresaCuestionario(Long empresaId, Long cuestionarioId, boolean activo) {
        this.id = new EmpresaCuestionarioId(empresaId, cuestionarioId);
        this.activo = activo;
    }

    public EmpresaCuestionarioId getId() {
        return id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

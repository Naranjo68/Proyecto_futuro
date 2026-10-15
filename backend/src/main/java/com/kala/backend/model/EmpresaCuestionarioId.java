package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EmpresaCuestionarioId implements Serializable {

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "cuestionario_id", nullable = false)
    private Long cuestionarioId;

    protected EmpresaCuestionarioId() {
        // Requerido por JPA.
    }

    public EmpresaCuestionarioId(Long empresaId, Long cuestionarioId) {
        this.empresaId = empresaId;
        this.cuestionarioId = cuestionarioId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public Long getCuestionarioId() {
        return cuestionarioId;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof EmpresaCuestionarioId id)) {
            return false;
        }
        return Objects.equals(empresaId, id.empresaId) && Objects.equals(cuestionarioId, id.cuestionarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empresaId, cuestionarioId);
    }
}

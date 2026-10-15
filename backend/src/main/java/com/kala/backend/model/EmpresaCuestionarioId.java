package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EmpresaCuestionarioId implements Serializable {

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "cuestionario_id")
    private Long cuestionarioId;

    public EmpresaCuestionarioId() {}

    public EmpresaCuestionarioId(Long empresaId, Long cuestionarioId) {
        this.empresaId = empresaId;
        this.cuestionarioId = cuestionarioId;
    }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public Long getCuestionarioId() { return cuestionarioId; }
    public void setCuestionarioId(Long cuestionarioId) { this.cuestionarioId = cuestionarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpresaCuestionarioId that = (EmpresaCuestionarioId) o;
        return Objects.equals(empresaId, that.empresaId) && Objects.equals(cuestionarioId, that.cuestionarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empresaId, cuestionarioId);
    }
}

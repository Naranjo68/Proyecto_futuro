package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PresupuestoPrivacidadId implements Serializable {

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "periodo", length = 7)
    private String periodo;

    public PresupuestoPrivacidadId() {}

    public PresupuestoPrivacidadId(Long empresaId, String periodo) {
        this.empresaId = empresaId;
        this.periodo = periodo;
    }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PresupuestoPrivacidadId that = (PresupuestoPrivacidadId) o;
        return Objects.equals(empresaId, that.empresaId) && Objects.equals(periodo, that.periodo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empresaId, periodo);
    }
}

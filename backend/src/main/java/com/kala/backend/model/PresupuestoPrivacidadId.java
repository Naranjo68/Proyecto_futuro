package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
public class PresupuestoPrivacidadId implements Serializable {

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // Mes en formato YYYY-MM; la columna es CHAR(7).
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "periodo", nullable = false, length = 7)
    private String periodo;

    protected PresupuestoPrivacidadId() {
        // Requerido por JPA.
    }

    public PresupuestoPrivacidadId(Long empresaId, String periodo) {
        this.empresaId = empresaId;
        this.periodo = periodo;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getPeriodo() {
        return periodo;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof PresupuestoPrivacidadId id)) {
            return false;
        }
        return Objects.equals(empresaId, id.empresaId) && Objects.equals(periodo, id.periodo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empresaId, periodo);
    }
}

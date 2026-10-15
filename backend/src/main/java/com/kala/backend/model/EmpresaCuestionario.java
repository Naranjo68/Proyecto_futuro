package com.kala.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "empresa_cuestionario")
public class EmpresaCuestionario {

    @EmbeddedId
    private EmpresaCuestionarioId id;

    @MapsId("cuestionarioId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @Column(nullable = false)
    private Boolean activo = true;

    public EmpresaCuestionario() {}

    public EmpresaCuestionario(Long empresaId, Cuestionario cuestionario, Boolean activo) {
        this.cuestionario = cuestionario;
        this.id = new EmpresaCuestionarioId(empresaId, cuestionario != null ? cuestionario.getId() : null);
        this.activo = activo;
    }

    public EmpresaCuestionarioId getId() { return id; }
    public void setId(EmpresaCuestionarioId id) { this.id = id; }

    public Cuestionario getCuestionario() { return cuestionario; }
    public void setCuestionario(Cuestionario cuestionario) { this.cuestionario = cuestionario; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}

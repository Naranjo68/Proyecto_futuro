package com.kala.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "presupuesto_privacidad")
public class PresupuestoPrivacidad {

    @EmbeddedId
    private PresupuestoPrivacidadId id;

    @Column(name = "epsilon_maximo_persona", nullable = false, precision = 6, scale = 3)
    private BigDecimal epsilonMaximoPersona;

    public PresupuestoPrivacidad() {}

    public PresupuestoPrivacidad(Long empresaId, String periodo, BigDecimal epsilonMaximoPersona) {
        this.id = new PresupuestoPrivacidadId(empresaId, periodo);
        this.epsilonMaximoPersona = epsilonMaximoPersona;
    }

    public PresupuestoPrivacidadId getId() { return id; }
    public void setId(PresupuestoPrivacidadId id) { this.id = id; }

    public BigDecimal getEpsilonMaximoPersona() { return epsilonMaximoPersona; }
    public void setEpsilonMaximoPersona(BigDecimal epsilonMaximoPersona) { this.epsilonMaximoPersona = epsilonMaximoPersona; }
}

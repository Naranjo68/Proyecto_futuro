package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

// Máximo de ε que una persona puede gastar en un mes dentro de una empresa. La empresa se referencia
// solo por id (la FK llega en V7).
@Entity
@Table(name = "presupuesto_privacidad")
public class PresupuestoPrivacidad {

    @EmbeddedId
    private PresupuestoPrivacidadId id;

    @Column(name = "epsilon_maximo_persona", nullable = false, precision = 6, scale = 3)
    private BigDecimal epsilonMaximoPersona;

    protected PresupuestoPrivacidad() {
        // Requerido por JPA.
    }

    public PresupuestoPrivacidad(Long empresaId, String periodo, BigDecimal epsilonMaximoPersona) {
        this.id = new PresupuestoPrivacidadId(empresaId, periodo);
        this.epsilonMaximoPersona = epsilonMaximoPersona;
    }

    public PresupuestoPrivacidadId getId() {
        return id;
    }

    public BigDecimal getEpsilonMaximoPersona() {
        return epsilonMaximoPersona;
    }
}

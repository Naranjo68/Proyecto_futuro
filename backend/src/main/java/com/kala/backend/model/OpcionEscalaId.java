package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OpcionEscalaId implements Serializable {

    @Column(name = "escala_id", nullable = false)
    private Long escalaId;

    @Column(name = "valor", nullable = false)
    private short valor;

    protected OpcionEscalaId() {
        // Requerido por JPA.
    }

    public OpcionEscalaId(Long escalaId, short valor) {
        this.escalaId = escalaId;
        this.valor = valor;
    }

    public Long getEscalaId() {
        return escalaId;
    }

    public short getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof OpcionEscalaId id)) {
            return false;
        }
        return valor == id.valor && Objects.equals(escalaId, id.escalaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(escalaId, valor);
    }
}

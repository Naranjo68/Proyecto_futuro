package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OpcionEscalaId implements Serializable {

    @Column(name = "escala_id")
    private Long escalaId;

    @Column(name = "valor")
    private Short valor;

    public OpcionEscalaId() {}

    public OpcionEscalaId(Long escalaId, Short valor) {
        this.escalaId = escalaId;
        this.valor = valor;
    }

    public Long getEscalaId() { return escalaId; }
    public void setEscalaId(Long escalaId) { this.escalaId = escalaId; }

    public Short getValor() { return valor; }
    public void setValor(Short valor) { this.valor = valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpcionEscalaId that = (OpcionEscalaId) o;
        return Objects.equals(escalaId, that.escalaId) && Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(escalaId, valor);
    }
}

package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "opcion_escala")
public class OpcionEscala {

    @EmbeddedId
    private OpcionEscalaId id;

    @Column(name = "etiqueta", nullable = false, length = 100)
    private String etiqueta;

    protected OpcionEscala() {
        // Requerido por JPA.
    }

    public OpcionEscala(Long escalaId, short valor, String etiqueta) {
        this.id = new OpcionEscalaId(escalaId, valor);
        this.etiqueta = etiqueta;
    }

    public OpcionEscalaId getId() {
        return id;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}

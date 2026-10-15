package com.kala.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "opcion_escala")
public class OpcionEscala {

    @EmbeddedId
    private OpcionEscalaId id;

    @MapsId("escalaId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_id", nullable = false)
    private EscalaRespuesta escalaRespuesta;

    @Column(nullable = false, length = 100)
    private String etiqueta;

    public OpcionEscala() {}

    public OpcionEscala(EscalaRespuesta escalaRespuesta, Short valor, String etiqueta) {
        this.escalaRespuesta = escalaRespuesta;
        this.id = new OpcionEscalaId(escalaRespuesta != null ? escalaRespuesta.getId() : null, valor);
        this.etiqueta = etiqueta;
    }

    public OpcionEscalaId getId() { return id; }
    public void setId(OpcionEscalaId id) { this.id = id; }

    public EscalaRespuesta getEscalaRespuesta() { return escalaRespuesta; }
    public void setEscalaRespuesta(EscalaRespuesta escalaRespuesta) { this.escalaRespuesta = escalaRespuesta; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
}

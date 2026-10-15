package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "escala_respuesta")
public class EscalaRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "valor_min", nullable = false)
    private short valorMin;

    @Column(name = "valor_max", nullable = false)
    private short valorMax;

    protected EscalaRespuesta() {
        // Requerido por JPA.
    }

    public EscalaRespuesta(String codigo, short valorMin, short valorMax) {
        this.codigo = codigo;
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public short getValorMin() {
        return valorMin;
    }

    public short getValorMax() {
        return valorMax;
    }
}

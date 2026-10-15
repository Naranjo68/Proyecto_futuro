package com.kala.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "escala_respuesta")
public class EscalaRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "valor_min", nullable = false)
    private Short valorMin;

    @Column(name = "valor_max", nullable = false)
    private Short valorMax;

    @OneToMany(mappedBy = "escalaRespuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionEscala> opciones = new ArrayList<>();

    public EscalaRespuesta() {}

    public EscalaRespuesta(String codigo, Short valorMin, Short valorMax) {
        this.codigo = codigo;
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Short getValorMin() { return valorMin; }
    public void setValorMin(Short valorMin) { this.valorMin = valorMin; }

    public Short getValorMax() { return valorMax; }
    public void setValorMax(Short valorMax) { this.valorMax = valorMax; }

    public List<OpcionEscala> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionEscala> opciones) { this.opciones = opciones; }
}

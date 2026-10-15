package com.kala.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "item_cuestionario")
public class ItemCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuestionario_id", nullable = false)
    private Cuestionario cuestionario;

    @Column(nullable = false)
    private Short orden;

    @Column(nullable = false, length = 100)
    private String tema;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escala_id", nullable = false)
    private EscalaRespuesta escalaRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracion_ldp_id", nullable = false)
    private ConfiguracionLdp configuracionLdp;

    public ItemCuestionario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cuestionario getCuestionario() { return cuestionario; }
    public void setCuestionario(Cuestionario cuestionario) { this.cuestionario = cuestionario; }

    public Short getOrden() { return orden; }
    public void setOrden(Short orden) { this.orden = orden; }

    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public EscalaRespuesta getEscalaRespuesta() { return escalaRespuesta; }
    public void setEscalaRespuesta(EscalaRespuesta escalaRespuesta) { this.escalaRespuesta = escalaRespuesta; }

    public ConfiguracionLdp getConfiguracionLdp() { return configuracionLdp; }
    public void setConfiguracionLdp(ConfiguracionLdp configuracionLdp) { this.configuracionLdp = configuracionLdp; }
}

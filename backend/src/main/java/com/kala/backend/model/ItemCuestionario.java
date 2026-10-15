package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_cuestionario")
public class ItemCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuestionario_id", nullable = false)
    private Long cuestionarioId;

    @Column(name = "orden", nullable = false)
    private short orden;

    @Column(name = "tema", nullable = false, length = 100)
    private String tema;

    @Column(name = "texto", nullable = false, columnDefinition = "text")
    private String texto;

    @Column(name = "escala_id", nullable = false)
    private Long escalaId;

    @Column(name = "configuracion_ldp_id", nullable = false)
    private Long configuracionLdpId;

    protected ItemCuestionario() {
        // Requerido por JPA.
    }

    public ItemCuestionario(Long cuestionarioId, short orden, String tema, String texto,
                            Long escalaId, Long configuracionLdpId) {
        this.cuestionarioId = cuestionarioId;
        this.orden = orden;
        this.tema = tema;
        this.texto = texto;
        this.escalaId = escalaId;
        this.configuracionLdpId = configuracionLdpId;
    }

    public Long getId() {
        return id;
    }

    public Long getCuestionarioId() {
        return cuestionarioId;
    }

    public short getOrden() {
        return orden;
    }

    public String getTema() {
        return tema;
    }

    public String getTexto() {
        return texto;
    }

    public Long getEscalaId() {
        return escalaId;
    }

    public Long getConfiguracionLdpId() {
        return configuracionLdpId;
    }
}

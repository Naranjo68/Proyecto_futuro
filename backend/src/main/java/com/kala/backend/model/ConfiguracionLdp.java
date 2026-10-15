package com.kala.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_ldp")
public class ConfiguracionLdp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 25)
    private String mecanismo;

    @Column(name = "probabilidad_verdad", precision = 4, scale = 3)
    private BigDecimal probabilidadVerdad;

    @Column(precision = 6, scale = 3)
    private BigDecimal epsilon;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    public ConfiguracionLdp() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMecanismo() { return mecanismo; }
    public void setMecanismo(String mecanismo) { this.mecanismo = mecanismo; }

    public BigDecimal getProbabilidadVerdad() { return probabilidadVerdad; }
    public void setProbabilidadVerdad(BigDecimal probabilidadVerdad) { this.probabilidadVerdad = probabilidadVerdad; }

    public BigDecimal getEpsilon() { return epsilon; }
    public void setEpsilon(BigDecimal epsilon) { this.epsilon = epsilon; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
}

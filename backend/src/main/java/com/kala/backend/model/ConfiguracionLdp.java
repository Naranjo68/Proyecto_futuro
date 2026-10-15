package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Parámetros con los que el dispositivo perturba una respuesta. El CHECK de la base exige que
// Randomized Response lleve probabilidad_verdad (0.5, 1) sin epsilon y que Laplace lleve epsilon sin probabilidad.
@Entity
@Table(name = "configuracion_ldp")
public class ConfiguracionLdp {

    public static final String RANDOMIZED_RESPONSE = "RANDOMIZED_RESPONSE";
    public static final String LAPLACE = "LAPLACE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mecanismo", nullable = false, length = 25)
    private String mecanismo;

    @Column(name = "probabilidad_verdad", precision = 4, scale = 3)
    private BigDecimal probabilidadVerdad;

    @Column(name = "epsilon", precision = 6, scale = 3)
    private BigDecimal epsilon;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    protected ConfiguracionLdp() {
        // Requerido por JPA.
    }

    public ConfiguracionLdp(String mecanismo, BigDecimal probabilidadVerdad, BigDecimal epsilon) {
        this.mecanismo = mecanismo;
        this.probabilidadVerdad = probabilidadVerdad;
        this.epsilon = epsilon;
    }

    public static ConfiguracionLdp randomizedResponse(BigDecimal probabilidadVerdad) {
        return new ConfiguracionLdp(RANDOMIZED_RESPONSE, probabilidadVerdad, null);
    }

    public static ConfiguracionLdp laplace(BigDecimal epsilon) {
        return new ConfiguracionLdp(LAPLACE, null, epsilon);
    }

    public Long getId() {
        return id;
    }

    public String getMecanismo() {
        return mecanismo;
    }

    public BigDecimal getProbabilidadVerdad() {
        return probabilidadVerdad;
    }

    public BigDecimal getEpsilon() {
        return epsilon;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}

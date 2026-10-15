package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Antiduplicado de un envío por persona y ventana. NO tiene referencia a usuario ni a equipo (R1, R6):
// solo el hash de un solo uso, el cuestionario y la fecha de inicio de la ventana (nunca una hora).
// Las inserciones se hacen con el INSERT ... ON CONFLICT de SesionCheckinRepository.
@Entity
@Table(name = "sesion_checkin")
public class SesionCheckin {

    // CHAR(64) en la base: sin esta anotación Hibernate esperaría VARCHAR y la validación del esquema fallaría.
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "session_hash", length = 64)
    private String sessionHash;

    @Column(name = "cuestionario_id", nullable = false)
    private Long cuestionarioId;

    @Column(name = "ventana_inicio", nullable = false)
    private LocalDate ventanaInicio;

    protected SesionCheckin() {
    }

    public String getSessionHash() {
        return sessionHash;
    }

    public Long getCuestionarioId() {
        return cuestionarioId;
    }

    public LocalDate getVentanaInicio() {
        return ventanaInicio;
    }
}

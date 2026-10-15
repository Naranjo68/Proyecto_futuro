
package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "consulta_dashboard")
public class ConsultaDashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;

    @Column(name = "resultado", nullable = false, length = 25)
    private String resultado;

    @Column(
        name = "consultado_en",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime consultadoEn;

    protected ConsultaDashboard() {
        // Requerido por JPA.
    }

    public ConsultaDashboard(
            Long usuarioId,
            Long equipoId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String resultado) {
        this.usuarioId = usuarioId;
        this.equipoId = equipoId;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.resultado = resultado;
    }

    @PrePersist
    private void validarRangoFechas() {
        if (fechaDesde == null || fechaHasta == null) {
            throw new IllegalStateException(
                "Las fechas de consulta son obligatorias"
            );
        }

        if (fechaHasta.isBefore(fechaDesde)) {
            throw new IllegalStateException(
                "La fecha final no puede ser anterior a la inicial"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getEquipoId() {
        return equipoId;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public String getResultado() {
        return resultado;
    }

    public LocalDateTime getConsultadoEn() {
        return consultadoEn;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}

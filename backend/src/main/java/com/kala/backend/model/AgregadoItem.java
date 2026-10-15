package com.kala.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

// Contadores ruidosos por equipo, ítem, configuración LDP y día. Guarda solo sumas: nunca la
// respuesta de una persona. Las referencias a otros módulos son Long, sin relaciones JPA.
// Las escrituras se hacen con el upsert nativo de AgregadoItemRepository, no desde esta entidad.
@Entity
@Table(name = "agregado_item")
public class AgregadoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "configuracion_ldp_id", nullable = false)
    private Long configuracionLdpId;

    @Column(nullable = false)
    private LocalDate fecha;

    // Randomized Response: cantidad de respuestas reportadas como 1.
    @Column(name = "suma_reportada", nullable = false, precision = 14, scale = 4)
    private BigDecimal sumaReportada;

    @Column(name = "total_respuestas", nullable = false)
    private int totalRespuestas;

    protected AgregadoItem() {
    }

    public Long getId() {
        return id;
    }

    public Long getEquipoId() {
        return equipoId;
    }

    public Long getItemId() {
        return itemId;
    }

    public Long getConfiguracionLdpId() {
        return configuracionLdpId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getSumaReportada() {
        return sumaReportada;
    }

    public int getTotalRespuestas() {
        return totalRespuestas;
    }
}

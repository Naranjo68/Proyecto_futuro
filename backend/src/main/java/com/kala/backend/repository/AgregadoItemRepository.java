package com.kala.backend.repository;

import com.kala.backend.model.AgregadoItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgregadoItemRepository extends JpaRepository<AgregadoItem, Long> {

    // Upsert atómico: crea el contador del día o le suma la respuesta, sin leer primero.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO agregado_item (equipo_id, item_id, configuracion_ldp_id, fecha, suma_reportada, total_respuestas)
            VALUES (:equipoId, :itemId, :configuracionLdpId, :fecha, :suma, 1)
            ON CONFLICT (equipo_id, item_id, configuracion_ldp_id, fecha) DO UPDATE SET
                suma_reportada   = agregado_item.suma_reportada + EXCLUDED.suma_reportada,
                total_respuestas = agregado_item.total_respuestas + 1
            """, nativeQuery = true)
    int registrarRespuesta(@Param("equipoId") Long equipoId,
                           @Param("itemId") Long itemId,
                           @Param("configuracionLdpId") Long configuracionLdpId,
                           @Param("fecha") LocalDate fecha,
                           @Param("suma") BigDecimal suma);

    Optional<AgregadoItem> findByEquipoIdAndItemIdAndConfiguracionLdpIdAndFecha(
            Long equipoId, Long itemId, Long configuracionLdpId, LocalDate fecha);
}

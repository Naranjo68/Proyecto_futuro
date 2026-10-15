package com.kala.backend.service;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

// Lee de item_cuestionario (tabla de otro módulo) solo lo que el check-in necesita saber del ítem.
// Se usa SQL directo con JdbcClient para no importar clases de ese módulo.
@Component
public class ItemCuestionarioLector {

    public record ItemCuestionarioInfo(Long cuestionarioId, Long configuracionLdpId) {
    }

    private final JdbcClient jdbc;

    public ItemCuestionarioLector(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ItemCuestionarioInfo> buscar(Long itemId) {
        return jdbc.sql("SELECT cuestionario_id, configuracion_ldp_id FROM item_cuestionario WHERE id = :itemId")
                .param("itemId", itemId)
                .query((rs, fila) -> new ItemCuestionarioInfo(rs.getLong("cuestionario_id"), rs.getLong("configuracion_ldp_id")))
                .optional();
    }
}

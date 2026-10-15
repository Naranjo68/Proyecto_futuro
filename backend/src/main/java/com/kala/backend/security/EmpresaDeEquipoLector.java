package com.kala.backend.security;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

// Lee de la tabla equipo (de otro módulo) solo la empresa a la que pertenece, con SQL directo
// para no importar clases de ese módulo.
@Component
public class EmpresaDeEquipoLector {

    private final JdbcClient jdbc;

    public EmpresaDeEquipoLector(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> empresaDelEquipo(Long equipoId) {
        return jdbc.sql("SELECT empresa_id FROM equipo WHERE id = :equipoId")
                .param("equipoId", equipoId)
                .query(Long.class)
                .optional();
    }
}

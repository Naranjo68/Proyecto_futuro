package com.kala.backend;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

// Crea las filas "padre" que exigen las claves foraneas de V7. Se usa dentro de pruebas con transaccion propia,
// de modo que todo se revierte al terminar cada prueba y no deja nada en la rama de Neon.
public final class FilasPadre {

    private FilasPadre() {
    }

    public record ItemCompleto(Long cuestionarioId, Long itemId, Long configuracionLdpId) {
    }

    public static Long empresa(JdbcTemplate jdbc) {
        String ruc = String.valueOf(10_000_000_000L + Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 89_999_999_999L));
        return jdbc.queryForObject(
                "INSERT INTO empresa (ruc, nombre, rubro) VALUES (?, 'Empresa de prueba', 'Pruebas') RETURNING id",
                Long.class, ruc);
    }

    public static Long equipo(JdbcTemplate jdbc, Long empresaId) {
        return jdbc.queryForObject(
                "INSERT INTO equipo (empresa_id, nombre) VALUES (?, ?) RETURNING id",
                Long.class, empresaId, "Equipo " + UUID.randomUUID());
    }

    public static Long usuario(JdbcTemplate jdbc) {
        return jdbc.queryForObject(
                "INSERT INTO usuario (email, password_hash, nombre) VALUES (?, 'hash-de-prueba', 'Persona de prueba') RETURNING id",
                Long.class, "u-" + UUID.randomUUID() + "@kala.test");
    }

    public static Long cuestionario(JdbcTemplate jdbc) {
        return jdbc.queryForObject(
                "INSERT INTO cuestionario (codigo, nombre, cadencia_dias) VALUES (?, 'Cuestionario de prueba', 7) RETURNING id",
                Long.class, "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 15));
    }

    // Un cuestionario con un item, su escala y una configuracion Randomized Response de p = 0.75.
    public static ItemCompleto item(JdbcTemplate jdbc) {
        Long escalaId = jdbc.queryForObject(
                "INSERT INTO escala_respuesta (codigo, valor_min, valor_max) VALUES (?, 0, 1) RETURNING id",
                Long.class, "E" + UUID.randomUUID().toString().replace("-", "").substring(0, 15));
        Long configId = jdbc.queryForObject(
                "INSERT INTO configuracion_ldp (mecanismo, probabilidad_verdad) VALUES ('RANDOMIZED_RESPONSE', 0.750) RETURNING id",
                Long.class);
        Long cuestionarioId = cuestionario(jdbc);
        Long itemId = jdbc.queryForObject(
                "INSERT INTO item_cuestionario (cuestionario_id, orden, tema, texto, escala_id, configuracion_ldp_id) "
                        + "VALUES (?, 1, 'Tema', 'Texto', ?, ?) RETURNING id",
                Long.class, cuestionarioId, escalaId, configId);
        return new ItemCompleto(cuestionarioId, itemId, configId);
    }
}

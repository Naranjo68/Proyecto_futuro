package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

// Auditoria de privacidad sobre el esquema real: las tablas de datos anonimos no deben poder unirse a una persona.
//  - agregado_item y sesion_checkin son las tablas "anonimas".
//  - usuario, empleado_equipo, personal_empresa y administrador_plataforma identifican personas.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AuditoriaPrivacidadTest {

    private static final Set<String> ANONIMAS = Set.of("agregado_item", "sesion_checkin");
    private static final Set<String> PERSONAS =
            Set.of("usuario", "empleado_equipo", "personal_empresa", "administrador_plataforma");

    @Autowired
    private JdbcTemplate jdbc;

    private List<Map<String, Object>> clavesForaneas() {
        return jdbc.queryForList("""
                SELECT c.conrelid::regclass::text AS origen, c.confrelid::regclass::text AS destino
                FROM pg_constraint c
                WHERE c.contype = 'f' AND c.connamespace = current_schema()::regnamespace
                """);
    }

    private Set<String> columnasDe(String tabla) {
        return jdbc.queryForList(
                        "SELECT column_name FROM information_schema.columns "
                                + "WHERE table_schema = current_schema() AND table_name = ?", String.class, tabla)
                .stream().collect(Collectors.toSet());
    }

    @Test
    void ningunaClaveForaneaUneUnaTablaDePersonasConUnaTablaAnonima() {
        List<Map<String, Object>> cruzadas = clavesForaneas().stream()
                .filter(fk -> (PERSONAS.contains(fk.get("origen")) && ANONIMAS.contains(fk.get("destino")))
                        || (ANONIMAS.contains(fk.get("origen")) && PERSONAS.contains(fk.get("destino"))))
                .toList();

        assertThat(cruzadas).as("claves foraneas entre personas y datos anonimos").isEmpty();
    }

    @Test
    void lasTablasAnonimasSoloReferencianLoQueDebenReferenciar() {
        Map<String, Set<String>> destinos = clavesForaneas().stream()
                .filter(fk -> ANONIMAS.contains((String) fk.get("origen")))
                .collect(Collectors.groupingBy(fk -> (String) fk.get("origen"),
                        Collectors.mapping(fk -> (String) fk.get("destino"), Collectors.toSet())));

        assertThat(destinos.get("agregado_item"))
                .containsExactlyInAnyOrder("equipo", "item_cuestionario", "configuracion_ldp");
        assertThat(destinos.get("sesion_checkin")).containsExactly("cuestionario");
    }

    @Test
    void lasTablasAnonimasTienenExactamenteLasColumnasEsperadas_ningunaUneAUnaPersona() {
        assertThat(columnasDe("agregado_item")).containsExactlyInAnyOrder(
                "id", "equipo_id", "item_id", "configuracion_ldp_id", "fecha", "suma_reportada", "total_respuestas");
        assertThat(columnasDe("sesion_checkin")).containsExactlyInAnyOrder(
                "session_hash", "cuestionario_id", "ventana_inicio");
    }

    @Test
    void lasTablasAnonimasNoTienenColumnasConHora() {
        List<String> conHora = jdbc.queryForList("""
                SELECT table_name || '.' || column_name
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name IN ('agregado_item', 'sesion_checkin')
                  AND (data_type LIKE 'timestamp%' OR data_type LIKE 'time%' OR data_type = 'interval')
                """, String.class);

        assertThat(conHora).as("columnas con hora en tablas anonimas").isEmpty();
    }
}

package com.kala.backend.repository;

import com.kala.backend.FilasPadre;
import com.kala.backend.model.ConsultaDashboard;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(
    named = "KALA_TEST_DB_URL",
    matches = ".+"
)
class ConsultaDashboardRepositoryTest {

    @Autowired
    private ConsultaDashboardRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    private Long usuarioId;
    private Long equipoId;

    // Las claves foraneas de V7 exigen usuario y equipo reales (se revierten al terminar cada prueba).
    @BeforeEach
    void crearPadres() {
        usuarioId = FilasPadre.usuario(jdbc);
        equipoId = FilasPadre.equipo(jdbc, FilasPadre.empresa(jdbc));
    }

    @Test
    void rechazaResultadoNoPermitido() {
        ConsultaDashboard consulta = new ConsultaDashboard(
            usuarioId,
            equipoId,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            "RESULTADO_INVALIDO"
        );

        assertThrows(
            DataIntegrityViolationException.class,
            () -> repository.saveAndFlush(consulta)
        );
    }

    @Test
    void guardaUnaConsultaValida_yLaBaseAsignaIdYFechaDeConsulta() {
        for (String resultado : new String[] {"OK", "DATOS_INSUFICIENTES"}) {
            ConsultaDashboard guardada = repository.saveAndFlush(new ConsultaDashboard(
                usuarioId, equipoId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 7), resultado));
            entityManager.clear();

            ConsultaDashboard recuperada = repository.findById(guardada.getId()).orElseThrow();
            assertEquals(resultado, recuperada.getResultado());
            assertNotNull(recuperada.getConsultadoEn());
        }
    }

    @Test
    void unRangoDeFechasInvertido_loRechazaLaEntidadAntesDeLlegarALaBase() {
        ConsultaDashboard invertida = new ConsultaDashboard(
            usuarioId, equipoId, LocalDate.of(2026, 1, 31), LocalDate.of(2026, 1, 1), "OK");

        assertThrows(Exception.class, () -> repository.saveAndFlush(invertida));
    }

    @Test
    void unRangoDeFechasInvertido_tambienLoRechazaElCheckDeLaBase() {
        assertThrows(
            DataIntegrityViolationException.class,
            () -> jdbc.update(
                "INSERT INTO consulta_dashboard (usuario_id, equipo_id, fecha_desde, fecha_hasta, resultado) "
                    + "VALUES (?, ?, DATE '2026-01-31', DATE '2026-01-01', 'OK')", usuarioId, equipoId)
        );
    }
}

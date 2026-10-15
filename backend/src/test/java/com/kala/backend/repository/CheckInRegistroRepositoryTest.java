package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.model.AgregadoItem;
import com.kala.backend.model.SesionCheckin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

// Necesita la rama de pruebas de Neon (KALA_TEST_DB_URL). Sin esa variable se omite.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CheckInRegistroRepositoryTest {

    private static final LocalDate DIA = LocalDate.of(2026, 10, 14);

    @Autowired
    private AgregadoItemRepository agregados;
    @Autowired
    private SesionCheckinRepository sesiones;

    private AgregadoItem buscar(LocalDate fecha) {
        return agregados.findByEquipoIdAndItemIdAndConfiguracionLdpIdAndFecha(900001L, 900002L, 900003L, fecha)
                .orElseThrow();
    }

    @Test
    void dosRespuestasDelMismoDia_seSumanEnUnaSolaFila() {
        agregados.registrarRespuesta(900001L, 900002L, 900003L, DIA, BigDecimal.ONE);
        agregados.registrarRespuesta(900001L, 900002L, 900003L, DIA, BigDecimal.ZERO);

        AgregadoItem agregado = buscar(DIA);
        assertThat(agregado.getTotalRespuestas()).isEqualTo(2);
        assertThat(agregado.getSumaReportada()).isEqualByComparingTo("1");
        assertThat(agregados.findAll().stream().filter(a -> a.getEquipoId().equals(900001L))).hasSize(1);
    }

    @Test
    void respuestasDeDiasDistintos_quedanEnFilasDistintas() {
        agregados.registrarRespuesta(900001L, 900002L, 900003L, DIA, BigDecimal.ONE);
        agregados.registrarRespuesta(900001L, 900002L, 900003L, DIA.plusDays(1), BigDecimal.ONE);

        assertThat(buscar(DIA).getTotalRespuestas()).isEqualTo(1);
        assertThat(buscar(DIA.plusDays(1)).getTotalRespuestas()).isEqualTo(1);
    }

    @Test
    void insertarSiNoExiste_devuelve1LaPrimeraVezY0LaSegunda() {
        String hash = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "").substring(0, 64);

        assertThat(sesiones.insertarSiNoExiste(hash, 3L, DIA)).isEqualTo(1);
        assertThat(sesiones.insertarSiNoExiste(hash, 3L, DIA)).isEqualTo(0);
    }

    @Test
    void laSesionGuardadaSoloTieneHashCuestionarioYFecha() {
        String hash = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "").substring(0, 64);
        sesiones.insertarSiNoExiste(hash, 3L, DIA);

        SesionCheckin sesion = sesiones.findById(hash).orElseThrow();
        assertThat(sesion.getSessionHash()).isEqualTo(hash);
        assertThat(sesion.getCuestionarioId()).isEqualTo(3L);
        assertThat(sesion.getVentanaInicio()).isEqualTo(DIA);
    }
}

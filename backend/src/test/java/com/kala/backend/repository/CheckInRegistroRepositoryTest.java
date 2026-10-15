package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.FilasPadre;
import com.kala.backend.FilasPadre.ItemCompleto;
import com.kala.backend.model.AgregadoItem;
import com.kala.backend.model.SesionCheckin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @Autowired
    private JdbcTemplate jdbc;

    private Long equipoId;
    private Long itemId;
    private Long configId;
    private Long cuestionarioId;

    // Las claves foraneas de V7 exigen equipo, item, configuracion y cuestionario reales (se revierten al terminar).
    @BeforeEach
    void crearPadres() {
        ItemCompleto item = FilasPadre.item(jdbc);
        equipoId = FilasPadre.equipo(jdbc, FilasPadre.empresa(jdbc));
        itemId = item.itemId();
        configId = item.configuracionLdpId();
        cuestionarioId = item.cuestionarioId();
    }

    private AgregadoItem buscar(LocalDate fecha) {
        return agregados.findByEquipoIdAndItemIdAndConfiguracionLdpIdAndFecha(equipoId, itemId, configId, fecha)
                .orElseThrow();
    }

    @Test
    void dosRespuestasDelMismoDia_seSumanEnUnaSolaFila() {
        agregados.registrarRespuesta(equipoId, itemId, configId, DIA, BigDecimal.ONE);
        agregados.registrarRespuesta(equipoId, itemId, configId, DIA, BigDecimal.ZERO);

        AgregadoItem agregado = buscar(DIA);
        assertThat(agregado.getTotalRespuestas()).isEqualTo(2);
        assertThat(agregado.getSumaReportada()).isEqualByComparingTo("1");
        assertThat(agregados.findAll().stream().filter(a -> a.getEquipoId().equals(equipoId))).hasSize(1);
    }

    @Test
    void respuestasDeDiasDistintos_quedanEnFilasDistintas() {
        agregados.registrarRespuesta(equipoId, itemId, configId, DIA, BigDecimal.ONE);
        agregados.registrarRespuesta(equipoId, itemId, configId, DIA.plusDays(1), BigDecimal.ONE);

        assertThat(buscar(DIA).getTotalRespuestas()).isEqualTo(1);
        assertThat(buscar(DIA.plusDays(1)).getTotalRespuestas()).isEqualTo(1);
    }

    @Test
    void insertarSiNoExiste_devuelve1LaPrimeraVezY0LaSegunda() {
        String hash = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "").substring(0, 64);

        assertThat(sesiones.insertarSiNoExiste(hash, cuestionarioId, DIA)).isEqualTo(1);
        assertThat(sesiones.insertarSiNoExiste(hash, cuestionarioId, DIA)).isEqualTo(0);
    }

    @Test
    void laSesionGuardadaSoloTieneHashCuestionarioYFecha() {
        String hash = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "").substring(0, 64);
        sesiones.insertarSiNoExiste(hash, cuestionarioId, DIA);

        SesionCheckin sesion = sesiones.findById(hash).orElseThrow();
        assertThat(sesion.getSessionHash()).isEqualTo(hash);
        assertThat(sesion.getCuestionarioId()).isEqualTo(cuestionarioId);
        assertThat(sesion.getVentanaInicio()).isEqualTo(DIA);
    }
}

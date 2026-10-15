package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kala.backend.FilasPadre;
import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.EmpresaCuestionario;
import com.kala.backend.model.EmpresaCuestionarioId;
import com.kala.backend.model.EscalaRespuesta;
import com.kala.backend.model.ItemCuestionario;
import com.kala.backend.model.OpcionEscala;
import com.kala.backend.model.OpcionEscalaId;
import com.kala.backend.model.PresupuestoPrivacidad;
import com.kala.backend.model.PresupuestoPrivacidadId;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
class InstrumentosRepositoryTest {

    @Autowired
    private EscalaRespuestaRepository escalas;
    @Autowired
    private OpcionEscalaRepository opciones;
    @Autowired
    private ConfiguracionLdpRepository configuraciones;
    @Autowired
    private CuestionarioRepository cuestionarios;
    @Autowired
    private ItemCuestionarioRepository items;
    @Autowired
    private EmpresaCuestionarioRepository activaciones;
    @Autowired
    private PresupuestoPrivacidadRepository presupuestos;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbc;

    private void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void guardaYRecuperaUnaEscalaConSusOpciones() {
        EscalaRespuesta escala = escalas.saveAndFlush(new EscalaRespuesta("LIKERT5_TEST", (short) 1, (short) 5));
        opciones.saveAndFlush(new OpcionEscala(escala.getId(), (short) 1, "Nunca"));
        opciones.saveAndFlush(new OpcionEscala(escala.getId(), (short) 5, "Siempre"));
        sincronizar();

        EscalaRespuesta leida = escalas.findById(escala.getId()).orElseThrow();
        assertThat(leida.getCodigo()).isEqualTo("LIKERT5_TEST");
        assertThat(leida.getValorMin()).isEqualTo((short) 1);
        assertThat(leida.getValorMax()).isEqualTo((short) 5);
        assertThat(opciones.listarPorEscala(escala.getId()))
                .extracting(o -> o.getId().getValor(), OpcionEscala::getEtiqueta)
                .containsExactly(org.assertj.core.groups.Tuple.tuple((short) 1, "Nunca"),
                        org.assertj.core.groups.Tuple.tuple((short) 5, "Siempre"));
        assertThat(opciones.findById(new OpcionEscalaId(escala.getId(), (short) 5))).isPresent();
    }

    @Test
    void guardaYRecuperaConfiguracionesDeAmbosMecanismos() {
        ConfiguracionLdp rr = configuraciones.saveAndFlush(
                ConfiguracionLdp.randomizedResponse(new BigDecimal("0.800")));
        ConfiguracionLdp laplace = configuraciones.saveAndFlush(ConfiguracionLdp.laplace(new BigDecimal("1.500")));
        sincronizar();

        ConfiguracionLdp leidaRr = configuraciones.findById(rr.getId()).orElseThrow();
        assertThat(leidaRr.getMecanismo()).isEqualTo("RANDOMIZED_RESPONSE");
        assertThat(leidaRr.getProbabilidadVerdad()).isEqualByComparingTo("0.800");
        assertThat(leidaRr.getEpsilon()).isNull();
        assertThat(leidaRr.getCreadoEn()).isNotNull();

        ConfiguracionLdp leidaLaplace = configuraciones.findById(laplace.getId()).orElseThrow();
        assertThat(leidaLaplace.getEpsilon()).isEqualByComparingTo("1.500");
        assertThat(leidaLaplace.getProbabilidadVerdad()).isNull();
    }

    @Test
    void laBaseRechazaProbabilidadUnMedio() {
        assertThatThrownBy(() -> configuraciones.saveAndFlush(
                ConfiguracionLdp.randomizedResponse(new BigDecimal("0.500"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laBaseRechazaProbabilidadUno() {
        assertThatThrownBy(() -> configuraciones.saveAndFlush(
                ConfiguracionLdp.randomizedResponse(new BigDecimal("1.000"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laBaseRechazaRandomizedResponseSinProbabilidad() {
        assertThatThrownBy(() -> configuraciones.saveAndFlush(
                ConfiguracionLdp.randomizedResponse(null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void laBaseRechazaLaplaceSinEpsilon() {
        assertThatThrownBy(() -> configuraciones.saveAndFlush(ConfiguracionLdp.laplace(null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void guardaYRecuperaUnCuestionarioConSusItems_yRechazaUnOrdenRepetido() {
        EscalaRespuesta escala = escalas.save(new EscalaRespuesta("ESCALA_ITEMS_TEST", (short) 0, (short) 1));
        ConfiguracionLdp config = configuraciones.save(ConfiguracionLdp.randomizedResponse(new BigDecimal("0.700")));
        Cuestionario cuestionario = cuestionarios.save(new Cuestionario("TEST_ITEMS", "Prueba", (short) 3, (short) 1));
        items.saveAndFlush(new ItemCuestionario(cuestionario.getId(), (short) 2, "B", "Segunda", escala.getId(), config.getId()));
        items.saveAndFlush(new ItemCuestionario(cuestionario.getId(), (short) 1, "A", "Primera", escala.getId(), config.getId()));
        sincronizar();

        Cuestionario leido = cuestionarios.findByCodigo("TEST_ITEMS").orElseThrow();
        assertThat(leido.getCadenciaDias()).isEqualTo((short) 3);
        assertThat(leido.getItemsPorEnvio()).isEqualTo((short) 1);
        assertThat(items.listarPorCuestionario(leido.getId()))
                .extracting(ItemCuestionario::getTema)
                .containsExactly("A", "B");

        assertThatThrownBy(() -> items.saveAndFlush(new ItemCuestionario(
                leido.getId(), (short) 1, "Otra", "Repetida", escala.getId(), config.getId())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void guardaYRecuperaUnaActivacionYUnPresupuesto() {
        Cuestionario cuestionario = cuestionarios.save(new Cuestionario("TEST_ACT", "Prueba", (short) 7, null));
        Long empresa = FilasPadre.empresa(jdbc);
        activaciones.saveAndFlush(new EmpresaCuestionario(empresa, cuestionario.getId(), true));
        presupuestos.saveAndFlush(new PresupuestoPrivacidad(empresa, "2026-03", new BigDecimal("12.500")));
        sincronizar();

        assertThat(activaciones.findById(new EmpresaCuestionarioId(empresa, cuestionario.getId())))
                .get().extracting(EmpresaCuestionario::isActivo).isEqualTo(true);
        assertThat(presupuestos.findById(new PresupuestoPrivacidadId(empresa, "2026-03")))
                .get().extracting(PresupuestoPrivacidad::getEpsilonMaximoPersona)
                .satisfies(eps -> assertThat(eps).isEqualByComparingTo("12.500"));
    }

    @Test
    void elSeedTieneElPulsoSemanalConRandomizedResponseConP075() {
        Cuestionario pulso = cuestionarios.findByCodigo("PULSO").orElseThrow();

        assertThat(pulso.getCadenciaDias()).isEqualTo((short) 7);
        assertThat(pulso.getItemsPorEnvio()).isNull();
        var itemsDelPulso = items.listarPorCuestionario(pulso.getId());
        assertThat(itemsDelPulso).hasSize(1);
        ConfiguracionLdp config = configuraciones.findById(itemsDelPulso.get(0).getConfiguracionLdpId()).orElseThrow();
        assertThat(config.getMecanismo()).isEqualTo("RANDOMIZED_RESPONSE");
        assertThat(config.getProbabilidadVerdad()).isEqualByComparingTo("0.750");
    }
}

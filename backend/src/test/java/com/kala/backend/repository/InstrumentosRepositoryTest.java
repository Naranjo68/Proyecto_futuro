package com.kala.backend.repository;

import com.kala.backend.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
class InstrumentosRepositoryTest {

    @Autowired
    private EscalaRespuestaRepository escalaRespuestaRepository;

    @Autowired
    private ConfiguracionLdpRepository configuracionLdpRepository;

    @Autowired
    private CuestionarioRepository cuestionarioRepository;

    @Autowired
    private EmpresaCuestionarioRepository empresaCuestionarioRepository;

    @Autowired
    private PresupuestoPrivacidadRepository presupuestoPrivacidadRepository;

    @Test
    @DisplayName("Guardar y recuperar entidades correctamente")
    void guardarYRecuperarEntidades() {
        EscalaRespuesta escala = new EscalaRespuesta("LIKERT_5", (short) 1, (short) 5);
        EscalaRespuesta escalaGuardada = escalaRespuestaRepository.save(escala);

        assertThat(escalaGuardada.getId()).isNotNull();
        Optional<EscalaRespuesta> recuperada = escalaRespuestaRepository.findByCodigo("LIKERT_5");
        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getValorMax()).isEqualTo((short) 5);
    }

    @Test
    @DisplayName("La base rechaza una configuración con p = 0.5")
    void rechazaConfiguracionProbabilidadP05() {
        ConfiguracionLdp config = new ConfiguracionLdp();
        config.setMecanismo("RANDOMIZED_RESPONSE");
        config.setProbabilidadVerdad(new BigDecimal("0.500"));

        assertThatThrownBy(() -> {
            configuracionLdpRepository.saveAndFlush(config);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("La base rechaza una configuración con p = 1.0")
    void rechazaConfiguracionProbabilidadP10() {
        ConfiguracionLdp config = new ConfiguracionLdp();
        config.setMecanismo("RANDOMIZED_RESPONSE");
        config.setProbabilidadVerdad(new BigDecimal("1.000"));

        assertThatThrownBy(() -> {
            configuracionLdpRepository.saveAndFlush(config);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("La base rechaza una configuración Laplace sin epsilon")
    void rechazaLaplaceSinEpsilon() {
        ConfiguracionLdp config = new ConfiguracionLdp();
        config.setMecanismo("LAPLACE");
        config.setEpsilon(null);

        assertThatThrownBy(() -> {
            configuracionLdpRepository.saveAndFlush(config);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Un ítem con un orden repetido dentro del mismo cuestionario es rechazado")
    void rechazaOrdenRepetidoEnMismoCuestionario() {
        Cuestionario cuestionario = cuestionarioRepository.findByCodigo("PULSO")
                .orElseThrow(() -> new IllegalStateException("Seed no cargado"));

        EscalaRespuesta escala = escalaRespuestaRepository.findByCodigo("BINARIA")
                .orElseThrow(() -> new IllegalStateException("Seed no cargado"));

        ConfiguracionLdp ldp = configuracionLdpRepository.findAll().get(0);

        ItemCuestionario itemDuplicado = new ItemCuestionario();
        itemDuplicado.setCuestionario(cuestionario);
        itemDuplicado.setOrden((short) 1);
        itemDuplicado.setTema("Prueba");
        itemDuplicado.setTexto("¿Texto duplicado?");
        itemDuplicado.setEscalaRespuesta(escala);
        itemDuplicado.setConfiguracionLdp(ldp);

        cuestionario.getItems().add(itemDuplicado);

        assertThatThrownBy(() -> {
            cuestionarioRepository.saveAndFlush(cuestionario);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Verifica que el seed exista (cuestionario PULSO con cadencia 7 y p = 0.750)")
    void verificaExistenciaDeSeed() {
        Optional<Cuestionario> cuestionarioOpt = cuestionarioRepository.findByCodigo("PULSO");
        assertThat(cuestionarioOpt).isPresent();

        Cuestionario cuestionario = cuestionarioOpt.get();
        assertThat(cuestionario.getCadenciaDias()).isEqualTo((short) 7);
        assertThat(cuestionario.getItems()).hasSize(1);

        ItemCuestionario item = cuestionario.getItems().get(0);
        assertThat(item.getTema()).isEqualTo("Pulso");
        assertThat(item.getConfiguracionLdp().getMecanismo()).isEqualTo("RANDOMIZED_RESPONSE");
        assertThat(item.getConfiguracionLdp().getProbabilidadVerdad()).isEqualByComparingTo(new BigDecimal("0.750"));
    }
}

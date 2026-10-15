package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.FilasPadre;
import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.EmpresaCuestionario;
import com.kala.backend.model.EscalaRespuesta;
import com.kala.backend.model.ItemCuestionario;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
class ConsultasJpqlInstrumentosTest {

    @Autowired
    private EscalaRespuestaRepository escalas;
    @Autowired
    private ConfiguracionLdpRepository configuraciones;
    @Autowired
    private CuestionarioRepository cuestionarios;
    @Autowired
    private ItemCuestionarioRepository items;
    @Autowired
    private EmpresaCuestionarioRepository activaciones;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void laConfiguracionVigenteDeUnItemEsLaQueElItemReferencia() {
        EscalaRespuesta escala = escalas.save(new EscalaRespuesta("ESCALA_JPQL_TEST", (short) 0, (short) 1));
        configuraciones.save(ConfiguracionLdp.randomizedResponse(new BigDecimal("0.600")));
        ConfiguracionLdp vigente = configuraciones.save(ConfiguracionLdp.randomizedResponse(new BigDecimal("0.900")));
        Cuestionario cuestionario = cuestionarios.save(new Cuestionario("TEST_JPQL", "Prueba", (short) 7, null));
        ItemCuestionario item = items.saveAndFlush(new ItemCuestionario(
                cuestionario.getId(), (short) 1, "Tema", "Texto", escala.getId(), vigente.getId()));

        assertThat(configuraciones.buscarVigenteDeItem(item.getId()))
                .get().extracting(ConfiguracionLdp::getProbabilidadVerdad)
                .satisfies(p -> assertThat(p).isEqualByComparingTo("0.900"));
        assertThat(configuraciones.buscarVigenteDeItem(-1L)).isEmpty();
    }

    @Test
    void losCuestionariosActivosDeUnaEmpresa_excluyenLosInactivosYLosDeOtraEmpresa() {
        Cuestionario activo = cuestionarios.save(new Cuestionario("TEST_ACTIVO", "Activo", (short) 7, null));
        Cuestionario inactivo = cuestionarios.save(new Cuestionario("TEST_INACTIVO", "Inactivo", (short) 7, null));
        Cuestionario deOtra = cuestionarios.save(new Cuestionario("TEST_OTRA", "De otra", (short) 7, null));
        Long empresa = FilasPadre.empresa(jdbc);
        Long otraEmpresa = FilasPadre.empresa(jdbc);
        Long empresaSinNada = FilasPadre.empresa(jdbc);
        activaciones.save(new EmpresaCuestionario(empresa, activo.getId(), true));
        activaciones.save(new EmpresaCuestionario(empresa, inactivo.getId(), false));
        activaciones.saveAndFlush(new EmpresaCuestionario(otraEmpresa, deOtra.getId(), true));

        assertThat(cuestionarios.listarActivosDeEmpresa(empresa))
                .extracting(Cuestionario::getCodigo)
                .containsExactly("TEST_ACTIVO");
        assertThat(cuestionarios.listarActivosDeEmpresa(empresaSinNada)).isEmpty();
    }
}

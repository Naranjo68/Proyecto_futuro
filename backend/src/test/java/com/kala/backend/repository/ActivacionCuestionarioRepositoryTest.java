package com.kala.backend.repository;

import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.EmpresaCuestionario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
class ActivacionCuestionarioRepositoryTest {

    @Autowired
    private ActivacionCuestionarioRepository activacionRepository;

    @Autowired
    private CuestionarioRepository cuestionarioRepository;

    @Test
    @DisplayName("Consulta JPQL recupera cuestionarios activos por empresa")
    void findCuestionariosActivosByEmpresaId() {
        Cuestionario cuestionario = cuestionarioRepository.findByCodigo("PULSO")
                .orElseThrow(() -> new IllegalStateException("Seed no cargado"));

        Long empresaId = 100L;
        EmpresaCuestionario ec = new EmpresaCuestionario(empresaId, cuestionario, true);
        activacionRepository.save(ec);

        List<Cuestionario> activos = activacionRepository.findCuestionariosActivosByEmpresaId(empresaId);
        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getCodigo()).isEqualTo("PULSO");
    }
}

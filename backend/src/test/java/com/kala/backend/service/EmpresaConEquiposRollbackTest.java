
package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kala.backend.dto.EmpresaConEquiposRequest;
import com.kala.backend.exception.NombreDuplicadoException;
import com.kala.backend.repository.EmpresaRepository;
import com.kala.backend.repository.EquipoRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(
        named = "KALA_TEST_DB_URL",
        matches = ".+")
@Import(EmpresaService.class)
class EmpresaConEquiposRollbackTest {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void crearConEquiposConNombreDuplicadoRevierteTodaLaOperacion() {
        long empresasAntes = empresaRepository.count();
        long equiposAntes = equipoRepository.count();

        String ruc = String.format(
                "%011d",
                Math.abs(System.nanoTime() % 100_000_000_000L));

        EmpresaConEquiposRequest request = new EmpresaConEquiposRequest(
                ruc,
                "Empresa rollback " + UUID.randomUUID(),
                "Pruebas",
                List.of("Desarrollo", "desarrollo"));

        assertThatThrownBy(() -> empresaService.crearConEquipos(request))
                .isInstanceOf(NombreDuplicadoException.class);

        assertThat(empresaRepository.count()).isEqualTo(empresasAntes);
        assertThat(equipoRepository.count()).isEqualTo(equiposAntes);
    }
}

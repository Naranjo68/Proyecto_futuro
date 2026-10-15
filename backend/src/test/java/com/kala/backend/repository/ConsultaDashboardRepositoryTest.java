package com.kala.backend.repository;

import com.kala.backend.model.ConsultaDashboard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

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

    @Test
    void rechazaResultadoNoPermitido() {
        ConsultaDashboard consulta = new ConsultaDashboard(
            1L,
            10L,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            "RESULTADO_INVALIDO"
        );

        assertThrows(
            DataIntegrityViolationException.class,
            () -> repository.saveAndFlush(consulta)
        );
    }
}

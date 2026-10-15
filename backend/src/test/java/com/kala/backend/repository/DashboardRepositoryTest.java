
package com.kala.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@JdbcTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(
        named = "KALA_TEST_DB_URL", matches = ".+")
@Import(DashboardRepository.class)
@Sql("/dashboard-fixtures.sql")
class DashboardRepositoryTest {

    @Autowired
    private DashboardRepository repository;

    @Test
    void agregaRespuestasDeTodaLaSemana() {
        var items = repository.consultar(
                100L,
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 10, 18));

        assertEquals(1, items.size());

        var item = items.get(0);

        assertEquals(1L, item.itemId());
        assertEquals("Bienestar", item.tema());
        assertEquals(30L, item.n());
        assertEquals(0, item.suma().compareTo(
                new java.math.BigDecimal("22")));
    }

    @Test
    void validaLaEmpresaDelEquipo() {
        assertTrue(repository.equipoPerteneceAEmpresa(100L, 10L));
        assertFalse(repository.equipoPerteneceAEmpresa(100L, 99L));
    }
}

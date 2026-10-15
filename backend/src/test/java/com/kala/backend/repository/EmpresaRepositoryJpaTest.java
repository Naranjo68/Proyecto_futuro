
package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.model.Empresa;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(
        named = "KALA_TEST_DB_URL",
        matches = ".+")
class EmpresaRepositoryJpaTest {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    void buscarPorNombreIgnoraMayusculasYOrdena() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        String marcador = "Jpql" + sufijo;
        String ruc = String.format(
                "%011d",
                Math.abs(System.nanoTime() % 100_000_000_000L));

        Empresa empresa = empresaRepository.save(
                new Empresa(ruc, marcador + " Empresa", "Tecnología"));

        try {
            var resultado = empresaRepository.buscarPorNombre(
                    marcador.toLowerCase());

            assertThat(resultado)
                    .extracting(Empresa::getId)
                    .contains(empresa.getId());

            assertThat(resultado)
                    .extracting(Empresa::getNombre)
                    .isSorted();
        } finally {
            empresaRepository.deleteById(empresa.getId());
        }
    }
}

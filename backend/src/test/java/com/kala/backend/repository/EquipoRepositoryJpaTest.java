
package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.model.Empresa;
import com.kala.backend.model.Equipo;
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
class EquipoRepositoryJpaTest {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Test
    void listarPorEmpresaOrdenadoDevuelveEquiposAlfabeticamente() {
        Empresa empresa = crearEmpresa();

        equipoRepository.save(new Equipo("Zeta", empresa.getId()));
        equipoRepository.save(new Equipo("Alfa", empresa.getId()));

        var resultado = equipoRepository.listarPorEmpresaOrdenado(
                empresa.getId());

        assertThat(resultado)
                .extracting(Equipo::getNombre)
                .containsExactly("Alfa", "Zeta");
    }

    @Test
    void contarPorEmpresaDevuelveCantidadDeEquipos() {
        Empresa empresa = crearEmpresa();

        equipoRepository.save(new Equipo("Equipo A", empresa.getId()));
        equipoRepository.save(new Equipo("Equipo B", empresa.getId()));

        assertThat(equipoRepository.contarPorEmpresa(empresa.getId()))
                .isEqualTo(2L);
    }

    private Empresa crearEmpresa() {
        String ruc = String.format(
                "%011d",
                Math.abs(System.nanoTime() % 100_000_000_000L));

        return empresaRepository.save(
                new Empresa(
                        ruc,
                        "Empresa-" + UUID.randomUUID().toString().substring(0, 8),
                        "Pruebas"));
    }
}

package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.Equipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Test unitario del service: JUnit 5 + AssertJ, sin Spring.
// EquipoService recibe un EmpresaService real, construido a mano en setUp().
class EquipoServiceTest {

    private EmpresaService empresaService;
    private EquipoService service;
    private Long empresaId;

    @BeforeEach
    void setUp() {
        empresaService = new EmpresaService();
        service = new EquipoService(empresaService);
        empresaId = empresaService.crear(new EmpresaRequest("ACME", "Tecnología")).getId();
    }

    @Test
    void crearEquipoValido_asignaIdYConservaEmpresaId() {
        Equipo creado = service.crear(new EquipoRequest("Equipo Ventas", empresaId));

        assertThat(creado.getId()).isNotNull();
        assertThat(creado.getNombre()).isEqualTo("Equipo Ventas");
        assertThat(creado.getEmpresaId()).isEqualTo(empresaId);
    }

    @Test
    void crearEquipoConEmpresaInexistente_lanzaEmpresaNoEncontradaException() {
        assertThatThrownBy(() -> service.crear(new EquipoRequest("Equipo huérfano", 999L)))
                .isInstanceOf(EmpresaNoEncontradaException.class);
    }

    @Test
    void crearEquipoConEmpresaIdNulo_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> service.crear(new EquipoRequest("Sin empresa", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listarPorEmpresa_filtraPorEmpresaIdODevuelveTodos() {
        Long otraEmpresa = empresaService.crear(new EmpresaRequest("Globex", "Retail")).getId();
        service.crear(new EquipoRequest("Equipo A", empresaId));
        service.crear(new EquipoRequest("Equipo B", empresaId));
        service.crear(new EquipoRequest("Equipo C", otraEmpresa));

        assertThat(service.listarPorEmpresa(empresaId))
                .extracting(Equipo::getNombre)
                .containsExactlyInAnyOrder("Equipo A", "Equipo B");

        assertThat(service.listarPorEmpresa(null)).hasSize(3);
    }

    @Test
    void buscarPorIdInexistente_lanzaEquipoNoEncontradoException() {
        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EquipoNoEncontradoException.class)
                .hasMessageContaining("999");
    }
}

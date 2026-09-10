package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.Equipo;
import org.junit.jupiter.api.Test;

/**
 * Fase RED (#15). Tests sin contexto de Spring: {@code new EquipoService()}.
 * Deben fallar / no compilar hasta que exista {@code EquipoService} (#16).
 *
 * En el Avance 1 el service NO inyecta {@code EmpresaService}: la validación
 * "la empresa debe existir de verdad" se difiere a integración (#25).
 */
class EquipoServiceTest {

    @Test
    void crearEquipoValido_asignaIdYConservaEmpresaId() {
        EquipoService service = new EquipoService();

        Equipo creado = service.crear(new EquipoRequest("Equipo Ventas", 1L));

        assertThat(creado.getId()).isNotNull();
        assertThat(creado.getNombre()).isEqualTo("Equipo Ventas");
        assertThat(creado.getEmpresaId()).isEqualTo(1L);
    }

    @Test
    void crearEquipoConEmpresaIdNulo_lanzaIllegalArgumentException() {
        EquipoService service = new EquipoService();

        assertThatThrownBy(() -> service.crear(new EquipoRequest("Equipo sin empresa", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listarPorEmpresa_filtraPorEmpresaIdODevuelveTodos() {
        EquipoService service = new EquipoService();
        service.crear(new EquipoRequest("Equipo A", 1L));
        service.crear(new EquipoRequest("Equipo B", 1L));
        service.crear(new EquipoRequest("Equipo C", 2L));

        assertThat(service.listarPorEmpresa(1L))
                .extracting(Equipo::getNombre)
                .containsExactlyInAnyOrder("Equipo A", "Equipo B");

        assertThat(service.listarPorEmpresa(null)).hasSize(3);
    }

    @Test
    void buscarPorIdInexistente_lanzaEquipoNoEncontradoException() {
        EquipoService service = new EquipoService();

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EquipoNoEncontradoException.class)
                .hasMessageContaining("999");
    }
}

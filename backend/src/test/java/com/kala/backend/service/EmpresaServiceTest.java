package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.model.Empresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Test unitario del service: JUnit 5 + AssertJ, sin Spring.
// EmpresaService no tiene dependencias.
class EmpresaServiceTest {

    private EmpresaService service;

    @BeforeEach
    void setUp() {
        service = new EmpresaService();
    }

    @Test
    void crearEmpresaValida_asignaIdYApareceEnListar() {
        Empresa creada = service.crear(new EmpresaRequest("ACME", "Tecnología"));

        assertThat(creada.getId()).isNotNull();
        assertThat(service.listar()).contains(creada);
    }

    @Test
    void buscarPorIdInexistente_lanzaEmpresaNoEncontradaExceptionConElId() {
        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EmpresaNoEncontradaException.class)
                .hasMessageContaining("999");
    }

    @Test
    void actualizarEmpresaExistente_reflejaLosNuevosValores() {
        Long id = service.crear(new EmpresaRequest("ACME", "Tecnología")).getId();

        Empresa actualizada = service.actualizar(id, new EmpresaRequest("ACME Corp", "Software"));

        assertThat(actualizada.getId()).isEqualTo(id);
        assertThat(actualizada.getNombre()).isEqualTo("ACME Corp");
        assertThat(actualizada.getRubro()).isEqualTo("Software");
    }

    @Test
    void eliminarEmpresaInexistente_lanzaEmpresaNoEncontradaException() {
        assertThatThrownBy(() -> service.eliminar(999L))
                .isInstanceOf(EmpresaNoEncontradaException.class);
    }
}

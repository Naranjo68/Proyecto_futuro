
package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.Empresa;
import com.kala.backend.model.Equipo;
import com.kala.backend.repository.EmpresaRepository;
import com.kala.backend.repository.EquipoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EquipoRepository equipoRepository;

    private EmpresaService empresaService;
    private EquipoService service;

    private final Long empresaId = 1L;

    @BeforeEach
    void setUp() {
        empresaService = new EmpresaService(empresaRepository, equipoRepository);
        service = new EquipoService(equipoRepository, empresaService);
    }

    private Empresa crearEmpresa() {
        Empresa empresa = new Empresa(empresaId, "ACME", "Tecnologia");
        empresa.setRuc("20123456789");
        return empresa;
    }

    @Test
    void crearEquipoValido_guardaEquipoConEmpresaId() {
        when(empresaRepository.findById(empresaId))
                .thenReturn(Optional.of(crearEmpresa()));
        when(equipoRepository.save(any(Equipo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Equipo creado = service.crear(
                new EquipoRequest("Equipo Ventas", empresaId)
        );

        assertThat(creado.getNombre()).isEqualTo("Equipo Ventas");
        assertThat(creado.getEmpresaId()).isEqualTo(empresaId);
        verify(equipoRepository).save(any(Equipo.class));
    }

    @Test
    void crearEquipoConEmpresaInexistente_lanzaEmpresaNoEncontradaException() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.crear(new EquipoRequest("Equipo huérfano", 999L)))
                .isInstanceOf(EmpresaNoEncontradaException.class);
    }

    @Test
    void crearEquipoConEmpresaIdNulo_lanzaIllegalArgumentException() {
        assertThatThrownBy(() ->
                service.crear(new EquipoRequest("Sin empresa", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empresaId");
    }

    @Test
    void listarPorEmpresa_filtraPorEmpresaId() {
        List<Equipo> equipos = List.of(
                new Equipo(1L, "Equipo A", empresaId),
                new Equipo(2L, "Equipo B", empresaId)
        );

        when(equipoRepository.findByEmpresaId(empresaId)).thenReturn(equipos);

        assertThat(service.listarPorEmpresa(empresaId))
                .extracting(Equipo::getNombre)
                .containsExactly("Equipo A", "Equipo B");
    }

    @Test
    void listarPorEmpresaSinFiltro_devuelveTodosLosEquipos() {
        List<Equipo> equipos = List.of(
                new Equipo(1L, "Equipo A", empresaId),
                new Equipo(2L, "Equipo B", 2L)
        );

        when(equipoRepository.findAll()).thenReturn(equipos);

        assertThat(service.listarPorEmpresa(null)).hasSize(2);
    }

    @Test
    void buscarPorIdInexistente_lanzaEquipoNoEncontradoException() {
        when(equipoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EquipoNoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    void actualizarEquipoExistente_guardaLosNuevosValores() {
        Equipo existente = new Equipo(5L, "Ventas", empresaId);

        when(empresaRepository.findById(empresaId))
                .thenReturn(Optional.of(crearEmpresa()));
        when(equipoRepository.findById(5L))
                .thenReturn(Optional.of(existente));
        when(equipoRepository.save(existente)).thenReturn(existente);

        Equipo actualizado = service.actualizar(
                5L, new EquipoRequest("Marketing", empresaId)
        );

        assertThat(actualizado.getNombre()).isEqualTo("Marketing");
        verify(equipoRepository).save(existente);
    }

    @Test
    void eliminarEquipoInexistente_lanzaEquipoNoEncontradoException() {
        when(equipoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(999L))
                .isInstanceOf(EquipoNoEncontradoException.class);
    }
}

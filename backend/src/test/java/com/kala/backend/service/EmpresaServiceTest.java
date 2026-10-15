

package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.exception.EmpresaConEquiposException;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.model.Empresa;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EquipoRepository equipoRepository;

    private EmpresaService service;

    @BeforeEach
    void setUp() {
        service = new EmpresaService(empresaRepository, equipoRepository);
    }

    @Test
    void listar_devuelveEmpresasDelRepositorio() {
        List<Empresa> empresas = List.of(
                new Empresa(1L, "ACME", "Tecnologia")
        );

        empresas.get(0).setRuc("20123456789");

        when(empresaRepository.findAll()).thenReturn(empresas);

        assertThat(service.listar()).isEqualTo(empresas);
    }

    @Test
    void crearEmpresaValida_guardaLaEmpresa() {
        EmpresaRequest request =
                new EmpresaRequest("20123456789", "ACME", "Tecnologia");

        when(empresaRepository.save(any(Empresa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Empresa creada = service.crear(request);

        assertThat(creada.getRuc()).isEqualTo("20123456789");
        assertThat(creada.getNombre()).isEqualTo("ACME");
        assertThat(creada.getRubro()).isEqualTo("Tecnologia");

        verify(empresaRepository).save(any(Empresa.class));
    }

    @Test
    void buscarPorIdInexistente_lanzaEmpresaNoEncontradaException() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(EmpresaNoEncontradaException.class)
                .hasMessageContaining("999");
    }

    @Test
    void actualizarEmpresaExistente_guardaLosNuevosValores() {
        Empresa existente =
                new Empresa(1L, "ACME", "Tecnologia");
        existente.setRuc("20123456789");

        EmpresaRequest request =
                new EmpresaRequest("20987654321", "ACME Corp", "Software");

        when(empresaRepository.findById(1L))
                .thenReturn(Optional.of(existente));
        when(empresaRepository.save(existente)).thenReturn(existente);

        Empresa actualizada = service.actualizar(1L, request);

        assertThat(actualizada.getId()).isEqualTo(1L);
        assertThat(actualizada.getRuc()).isEqualTo("20987654321");
        assertThat(actualizada.getNombre()).isEqualTo("ACME Corp");
        assertThat(actualizada.getRubro()).isEqualTo("Software");

        verify(empresaRepository).save(existente);
    }

    @Test
    void eliminarEmpresaInexistente_lanzaEmpresaNoEncontradaException() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(999L))
                .isInstanceOf(EmpresaNoEncontradaException.class);
    }

    @Test
    void eliminarEmpresaConEquipos_lanzaConflicto() {
        Empresa empresa =
                new Empresa(1L, "ACME", "Tecnologia");
        empresa.setRuc("20123456789");

        when(empresaRepository.findById(1L))
                .thenReturn(Optional.of(empresa));
        when(equipoRepository.existsByEmpresaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(1L))
                .isInstanceOf(EmpresaConEquiposException.class)
                .hasMessageContaining("equipos asociados");
    }
}

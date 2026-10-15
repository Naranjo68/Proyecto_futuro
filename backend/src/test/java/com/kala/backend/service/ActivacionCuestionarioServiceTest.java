package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kala.backend.exception.CuestionarioNoEncontradoException;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.exception.PresupuestoExcedidoException;
import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.EmpresaCuestionario;
import com.kala.backend.model.ItemCuestionario;
import com.kala.backend.model.PresupuestoPrivacidad;
import com.kala.backend.repository.ConfiguracionLdpRepository;
import com.kala.backend.repository.CuestionarioRepository;
import com.kala.backend.repository.EmpresaCuestionarioRepository;
import com.kala.backend.repository.EmpresaRepository;
import com.kala.backend.repository.ItemCuestionarioRepository;
import com.kala.backend.repository.PresupuestoPrivacidadRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ActivacionCuestionarioServiceTest {

    private static final Long EMPRESA = 1L;

    private final EmpresaRepository empresas = mock(EmpresaRepository.class);
    private final CuestionarioRepository cuestionarios = mock(CuestionarioRepository.class);
    private final ItemCuestionarioRepository items = mock(ItemCuestionarioRepository.class);
    private final ConfiguracionLdpRepository configuraciones = mock(ConfiguracionLdpRepository.class);
    private final EmpresaCuestionarioRepository activaciones = mock(EmpresaCuestionarioRepository.class);
    private final PresupuestoPrivacidadRepository presupuestos = mock(PresupuestoPrivacidadRepository.class);
    // 15 de marzo de 2026, hora de Lima
    private final Clock reloj = Clock.fixed(Instant.parse("2026-03-15T15:00:00Z"), ZoneId.of("America/Lima"));

    private ActivacionCuestionarioService service;

    @BeforeEach
    void preparar() {
        service = new ActivacionCuestionarioService(empresas, cuestionarios, items, configuraciones,
                activaciones, presupuestos, reloj, 20.0);
        when(empresas.existsById(EMPRESA)).thenReturn(true);
        when(cuestionarios.listarActivosDeEmpresa(EMPRESA)).thenReturn(List.of());
        when(activaciones.findById(any())).thenReturn(Optional.empty());
        when(presupuestos.findById(any())).thenReturn(Optional.empty());
    }

    // Cuestionario con un solo ítem de Randomized Response con p = 0.75 y la cadencia indicada.
    private Cuestionario cuestionarioConUnItem(Long id, int cadenciaDias) {
        Cuestionario c = new Cuestionario("C" + id, "Cuestionario " + id, (short) cadenciaDias, null);
        ReflectionTestUtils.setField(c, "id", id);
        ItemCuestionario item = new ItemCuestionario(id, (short) 1, "Tema", "Texto", 1L, 7L);
        ReflectionTestUtils.setField(item, "id", id * 10);
        when(cuestionarios.findById(id)).thenReturn(Optional.of(c));
        when(items.listarPorCuestionario(id)).thenReturn(List.of(item));
        when(configuraciones.findById(7L)).thenReturn(
                Optional.of(ConfiguracionLdp.randomizedResponse(new BigDecimal("0.750"))));
        return c;
    }

    @Test
    void dentroDelLimite_activaYGuarda() {
        cuestionarioConUnItem(10L, 7);

        var resultado = service.activar(EMPRESA, 10L);

        assertThat(resultado.epsilonMensual()).isCloseTo(5.49, org.assertj.core.data.Offset.offset(0.01));
        assertThat(resultado.epsilonMaximo()).isEqualTo(20.0);
        verify(activaciones).save(any(EmpresaCuestionario.class));
    }

    @Test
    void siExcedeElMaximo_lanza422YNoGuardaNada() {
        cuestionarioConUnItem(10L, 1);

        assertThatThrownBy(() -> service.activar(EMPRESA, 10L))
                .isInstanceOf(PresupuestoExcedidoException.class);
        verify(activaciones, never()).save(any());
    }

    @Test
    void sumaLoYaActivoConLoNuevo() {
        Cuestionario activo = cuestionarioConUnItem(10L, 7);
        when(cuestionarios.listarActivosDeEmpresa(EMPRESA)).thenReturn(List.of(activo));
        cuestionarioConUnItem(11L, 7);

        var resultado = service.activar(EMPRESA, 11L);

        assertThat(resultado.epsilonMensual()).isCloseTo(10.99, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void reactivarElMismoCuestionario_noDuplicaElGasto() {
        Cuestionario activo = cuestionarioConUnItem(10L, 7);
        when(cuestionarios.listarActivosDeEmpresa(EMPRESA)).thenReturn(List.of(activo));

        var resultado = service.activar(EMPRESA, 10L);

        assertThat(resultado.epsilonMensual()).isCloseTo(5.49, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void laFilaDePresupuestoDelMesTienePrioridadSobreElValorPorDefecto() {
        cuestionarioConUnItem(10L, 7);
        when(presupuestos.findById(new com.kala.backend.model.PresupuestoPrivacidadId(EMPRESA, "2026-03")))
                .thenReturn(Optional.of(new PresupuestoPrivacidad(EMPRESA, "2026-03", new BigDecimal("5.000"))));

        assertThatThrownBy(() -> service.activar(EMPRESA, 10L))
                .isInstanceOf(PresupuestoExcedidoException.class);
        verify(activaciones, never()).save(any());
    }

    @Test
    void empresaOCuestionarioInexistente_son404() {
        when(empresas.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.activar(99L, 10L)).isInstanceOf(EmpresaNoEncontradaException.class);

        when(cuestionarios.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.activar(EMPRESA, 5L)).isInstanceOf(CuestionarioNoEncontradoException.class);
        verify(activaciones, never()).save(any());
    }
}

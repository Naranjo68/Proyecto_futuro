package com.kala.backend.service;

import com.kala.backend.exception.PresupuestoExcedidoException;
import com.kala.backend.model.Cuestionario;

import com.kala.backend.model.PresupuestoPrivacidad;
import com.kala.backend.model.PresupuestoPrivacidadId;
import com.kala.backend.repository.ActivacionCuestionarioRepository;
import com.kala.backend.repository.CuestionarioRepository;
import com.kala.backend.repository.PresupuestoPrivacidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ActivacionCuestionarioServiceTest {

    private ActivacionCuestionarioRepository activacionRepository;
    private CuestionarioRepository cuestionarioRepository;
    private PresupuestoPrivacidadRepository presupuestoRepository;
    private CalculadoraPresupuesto calculadora;
    private ActivacionCuestionarioService service;

    @BeforeEach
    void setUp() {
        activacionRepository = mock(ActivacionCuestionarioRepository.class);
        cuestionarioRepository = mock(CuestionarioRepository.class);
        presupuestoRepository = mock(PresupuestoPrivacidadRepository.class);
        calculadora = mock(CalculadoraPresupuesto.class);

        service = new ActivacionCuestionarioService(
                activacionRepository, cuestionarioRepository, presupuestoRepository, calculadora, 10.0
        );
    }

    @Test
    @DisplayName("Dentro del límite activa el cuestionario correctamente")
    void dentroDelLimiteActiva() {
        Long empresaId = 1L;
        Long cuestionarioId = 10L;

        Cuestionario nuevo = new Cuestionario();
        given(cuestionarioRepository.findById(cuestionarioId)).willReturn(Optional.of(nuevo));
        given(activacionRepository.findCuestionariosActivosByEmpresaId(empresaId)).willReturn(Collections.emptyList());
        given(calculadora.calcularEpsilonMensualCuestionario(nuevo)).willReturn(5.0);

        service.activar(empresaId, cuestionarioId);

        verify(activacionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Si excede el presupuesto lanza 422 PresupuestoExcedidoException y nada se guarda")
    void excedePresupuestoLanzaExcepcion() {
        Long empresaId = 1L;
        Long cuestionarioId = 10L;

        Cuestionario nuevo = new Cuestionario();
        given(cuestionarioRepository.findById(cuestionarioId)).willReturn(Optional.of(nuevo));
        given(activacionRepository.findCuestionariosActivosByEmpresaId(empresaId)).willReturn(Collections.emptyList());
        given(calculadora.calcularEpsilonMensualCuestionario(nuevo)).willReturn(15.0); // Excede el límite de 10.0

        assertThatThrownBy(() -> service.activar(empresaId, cuestionarioId))
                .isInstanceOf(PresupuestoExcedidoException.class);

        verify(activacionRepository, never()).save(any());
    }
}

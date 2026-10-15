package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.exception.SemanaInvalidaException;
import com.kala.backend.model.ConsultaDashboard;
import com.kala.backend.repository.ConsultaDashboardRepository;
import com.kala.backend.repository.DashboardRepository;
import com.kala.backend.repository.DashboardRepository.FilaItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DashboardServiceTest {

    private static final Long USUARIO = 5L;
    private static final Long EMPRESA = 1L;
    private static final Long EQUIPO = 10L;
    private static final LocalDate LUNES = LocalDate.of(2026, 3, 9);

    private final DashboardRepository repositorio = mock(DashboardRepository.class);
    private final ConsultaDashboardRepository auditoria = mock(ConsultaDashboardRepository.class);
    private final DashboardService service = new DashboardService(repositorio, auditoria, 30);

    private FilaItem fila(int si, int n) {
        return fila("RANDOMIZED_RESPONSE", new BigDecimal("0.750"), si, n);
    }

    private FilaItem fila(String mecanismo, BigDecimal p, int si, int n) {
        return new FilaItem(1L, "Pulso", 2L, mecanismo, p, null, 0, 1, new BigDecimal(si), n);
    }

    private ConsultaDashboard auditoriaGuardada() {
        ArgumentCaptor<ConsultaDashboard> captor = ArgumentCaptor.forClass(ConsultaDashboard.class);
        verify(auditoria).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void con29Respuestas_devuelveDatosInsuficientesSinProporcion_yAudita() {
        when(repositorio.equipoPerteneceAEmpresa(EQUIPO, EMPRESA)).thenReturn(true);
        when(repositorio.agregadosDeVentana(EQUIPO, LUNES, LUNES.plusDays(6))).thenReturn(List.of(fila(20, 29)));

        var r = service.consultar(USUARIO, EMPRESA, EQUIPO, LUNES);

        assertThat(r.resultado()).isEqualTo("DATOS_INSUFICIENTES");
        assertThat(r.totalRespuestas()).isEqualTo(29);
        assertThat(r.items()).hasSize(1);
        assertThat(r.items().get(0).proporcionEstimada()).isNull();
        assertThat(r.items().get(0).margenError95()).isNull();
        assertThat(auditoriaGuardada().getResultado()).isEqualTo("DATOS_INSUFICIENTES");
    }

    @Test
    void con30Respuestas_devuelveOkConProporcionYMargen_yAudita() {
        when(repositorio.equipoPerteneceAEmpresa(EQUIPO, EMPRESA)).thenReturn(true);
        // λ = 21 / 30 = 0.70 -> π = (0.70 − 0.25) / 0.5 = 0.90
        when(repositorio.agregadosDeVentana(EQUIPO, LUNES, LUNES.plusDays(6))).thenReturn(List.of(fila(21, 30)));

        var r = service.consultar(USUARIO, EMPRESA, EQUIPO, LUNES);

        assertThat(r.resultado()).isEqualTo("OK");
        assertThat(r.items().get(0).proporcionEstimada()).isEqualTo(0.90, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(r.items().get(0).margenError95()).isPositive();
        ConsultaDashboard guardada = auditoriaGuardada();
        assertThat(guardada.getResultado()).isEqualTo("OK");
        assertThat(guardada.getUsuarioId()).isEqualTo(USUARIO);
        assertThat(guardada.getEquipoId()).isEqualTo(EQUIPO);
        assertThat(guardada.getFechaDesde()).isEqualTo(LUNES);
        assertThat(guardada.getFechaHasta()).isEqualTo(LUNES.plusDays(6));
    }

    @Test
    void sinNingunaRespuesta_esDatosInsuficientes_yAudita() {
        when(repositorio.equipoPerteneceAEmpresa(EQUIPO, EMPRESA)).thenReturn(true);
        when(repositorio.agregadosDeVentana(EQUIPO, LUNES, LUNES.plusDays(6))).thenReturn(List.of());

        var r = service.consultar(USUARIO, EMPRESA, EQUIPO, LUNES);

        assertThat(r.resultado()).isEqualTo("DATOS_INSUFICIENTES");
        assertThat(r.totalRespuestas()).isZero();
        assertThat(auditoriaGuardada().getResultado()).isEqualTo("DATOS_INSUFICIENTES");
    }

    @Test
    void unItemConMecanismoLaplace_noSeEstimaComoRandomizedResponse() {
        when(repositorio.equipoPerteneceAEmpresa(EQUIPO, EMPRESA)).thenReturn(true);
        when(repositorio.agregadosDeVentana(EQUIPO, LUNES, LUNES.plusDays(6)))
                .thenReturn(List.of(fila("LAPLACE", null, 21, 30)));

        var r = service.consultar(USUARIO, EMPRESA, EQUIPO, LUNES);

        assertThat(r.items().get(0).proporcionEstimada()).isNull();
    }

    @Test
    void unaSemanaQueNoEmpiezaEnLunes_esSemanaInvalida_yNoAudita() {
        assertThatThrownBy(() -> service.consultar(USUARIO, EMPRESA, EQUIPO, LUNES.plusDays(1)))
                .isInstanceOf(SemanaInvalidaException.class);

        verify(auditoria, never()).save(any());
    }

    @Test
    void unEquipoDeOtraEmpresa_es404_yNoAudita() {
        when(repositorio.equipoPerteneceAEmpresa(EQUIPO, EMPRESA)).thenReturn(false);

        assertThatThrownBy(() -> service.consultar(USUARIO, EMPRESA, EQUIPO, LUNES))
                .isInstanceOf(EquipoNoEncontradoException.class);

        verify(auditoria, never()).save(any());
        verify(repositorio, never()).agregadosDeVentana(any(), any(), any());
    }
}

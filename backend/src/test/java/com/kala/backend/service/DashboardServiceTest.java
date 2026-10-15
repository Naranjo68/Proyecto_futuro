
package com.kala.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.kala.backend.exception.SemanaInvalidaException;
import com.kala.backend.repository.DashboardRepository;
import com.kala.backend.repository.DashboardRepository.ItemAgregado;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository repository;

    @Mock
    private EstimadorDesesgado estimador;

    private DashboardService service;

    private final Long usuarioId = 1L;
    private final Long empresaId = 10L;
    private final Long equipoId = 100L;
    private final LocalDate lunes = LocalDate.of(2026, 10, 12);

    @BeforeEach
    void setUp() {
        service = new DashboardService(repository, estimador, 30);
    }

    @Test
    void con29RespuestasNoExponeLaProporcionYAudita() {
        when(repository.equipoPerteneceAEmpresa(equipoId, empresaId))
                .thenReturn(true);

        when(repository.consultar(equipoId, lunes, lunes.plusDays(6)))
                .thenReturn(List.of(item(29, 15)));

        var resultado = service.consultar(
                usuarioId, empresaId, equipoId, lunes);

        assertEquals("DATOS_INSUFICIENTES", resultado.resultado());
        assertEquals("DATOS_INSUFICIENTES",
                resultado.items().get(0).resultado());
        assertNull(resultado.items().get(0).proporcionEstimada());
        assertNull(resultado.items().get(0).margenError95());

        verify(repository).registrarConsulta(
                usuarioId, equipoId, lunes, lunes.plusDays(6),
                "DATOS_INSUFICIENTES");

        verifyNoInteractions(estimador);
    }

    @Test
    void con30RespuestasCalculaYAudita() {
        when(repository.equipoPerteneceAEmpresa(equipoId, empresaId))
                .thenReturn(true);

        when(repository.consultar(equipoId, lunes, lunes.plusDays(6)))
                .thenReturn(List.of(item(30, 12)));

        when(estimador.estimarProporcion(12, 30)).thenReturn(0.30);
        when(estimador.margenError95(12, 30)).thenReturn(0.20);

        var resultado = service.consultar(
                usuarioId, empresaId, equipoId, lunes);

        assertEquals("OK", resultado.resultado());
        assertEquals(0.30,
                resultado.items().get(0).proporcionEstimada());
        assertEquals(0.20,
                resultado.items().get(0).margenError95());

        verify(repository).registrarConsulta(
                usuarioId, equipoId, lunes, lunes.plusDays(6), "OK");
    }

    @Test
    void semanaQueNoEsLunesDevuelve400YSinAuditar() {
        LocalDate martes = lunes.plusDays(1);

        assertThrows(SemanaInvalidaException.class,
                () -> service.consultar(
                        usuarioId, empresaId, equipoId, martes));

        verifyNoInteractions(repository);
        verifyNoInteractions(estimador);
    }

    @Test
    void equipoDeOtraEmpresaDevuelve404YSinAuditar() {
        when(repository.equipoPerteneceAEmpresa(equipoId, empresaId))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        usuarioId, empresaId, equipoId, lunes));

        assertEquals(404, ex.getStatusCode().value());

        verify(repository, never())
                .registrarConsulta(any(), any(), any(), any(), any());
    }

    @Test
    void sumaFraccionariaFallaEnLugarDeTruncarse() {
        when(repository.equipoPerteneceAEmpresa(equipoId, empresaId))
                .thenReturn(true);

        when(repository.consultar(equipoId, lunes, lunes.plusDays(6)))
                .thenReturn(List.of(item(30, 12)
                        .conSuma(new BigDecimal("12.5"))));

        assertThrows(IllegalStateException.class,
                () -> service.consultar(
                        usuarioId, empresaId, equipoId, lunes));

        verify(repository, never())
                .registrarConsulta(any(), any(), any(), any(), any());
    }

    private ItemAgregado item(long n, int suma) {
        return new ItemAgregado(
                1L,
                "Bienestar",
                5L,
                "RANDOMIZED_RESPONSE",
                new BigDecimal("0.750"),
                new BigDecimal("1.000"),
                0,
                1,
                BigDecimal.valueOf(suma),
                n);
    }
}

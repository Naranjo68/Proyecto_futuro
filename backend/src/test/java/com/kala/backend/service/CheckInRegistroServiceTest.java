package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kala.backend.dto.CheckInEnvioRequest;
import com.kala.backend.exception.CheckInDuplicadoException;
import com.kala.backend.exception.ConfiguracionLdpNoVigenteException;
import com.kala.backend.exception.ItemCuestionarioNoEncontradoException;
import com.kala.backend.repository.AgregadoItemRepository;
import com.kala.backend.repository.SesionCheckinRepository;
import com.kala.backend.service.ItemCuestionarioLector.ItemCuestionarioInfo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CheckInRegistroServiceTest {

    private static final String SECRETO = "secreto-de-prueba";
    private static final String ZONA = "America/Lima";
    private static final LocalDate LUNES = LocalDate.of(2026, 10, 12);
    private static final LocalDate MIERCOLES = LocalDate.of(2026, 10, 14);

    // 2026-10-14T15:00Z = miércoles 10:00 en Lima; su ventana empieza el lunes 2026-10-12.
    private static final Clock MIERCOLES_EN_LIMA = Clock.fixed(Instant.parse("2026-10-14T15:00:00Z"), ZoneOffset.UTC);

    private ItemCuestionarioLector items;
    private SesionCheckinRepository sesiones;
    private AgregadoItemRepository agregados;
    private CheckInRegistroService service;

    @BeforeEach
    void setUp() {
        items = mock(ItemCuestionarioLector.class);
        sesiones = mock(SesionCheckinRepository.class);
        agregados = mock(AgregadoItemRepository.class);
        service = new CheckInRegistroService(items, sesiones, agregados, SECRETO, ZONA, MIERCOLES_EN_LIMA);
    }

    private void itemVigente() {
        when(items.buscar(9L)).thenReturn(Optional.of(new ItemCuestionarioInfo(3L, 2L)));
    }

    @Test
    void respuestaVerdadera_guardaLaSesionYSumaUnoAlAgregadoDelDia() {
        itemVigente();
        when(sesiones.insertarSiNoExiste(anyString(), eq(3L), eq(LUNES))).thenReturn(1);

        service.registrar(7L, 5L, new CheckInEnvioRequest(9L, 2L, true));

        verify(sesiones).insertarSiNoExiste(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, LUNES), 3L, LUNES);
        verify(agregados).registrarRespuesta(5L, 9L, 2L, MIERCOLES, BigDecimal.ONE);
    }

    @Test
    void respuestaFalsa_cuentaEnElTotalPeroSumaCero() {
        itemVigente();
        when(sesiones.insertarSiNoExiste(anyString(), eq(3L), eq(LUNES))).thenReturn(1);

        service.registrar(7L, 5L, new CheckInEnvioRequest(9L, 2L, false));

        verify(agregados).registrarRespuesta(5L, 9L, 2L, MIERCOLES, BigDecimal.ZERO);
    }

    @Test
    void siLaSesionYaExiste_lanza409YNoTocaElAgregado() {
        itemVigente();
        when(sesiones.insertarSiNoExiste(anyString(), eq(3L), eq(LUNES))).thenReturn(0);

        assertThatThrownBy(() -> service.registrar(7L, 5L, new CheckInEnvioRequest(9L, 2L, true)))
                .isInstanceOf(CheckInDuplicadoException.class);

        verifyNoInteractions(agregados);
    }

    @Test
    void siLaConfiguracionNoEsLaVigente_lanzaYNoGuardaNada() {
        itemVigente();

        assertThatThrownBy(() -> service.registrar(7L, 5L, new CheckInEnvioRequest(9L, 99L, true)))
                .isInstanceOf(ConfiguracionLdpNoVigenteException.class);

        verifyNoInteractions(sesiones, agregados);
    }

    @Test
    void siElItemNoExiste_lanzaYNoGuardaNada() {
        when(items.buscar(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(7L, 5L, new CheckInEnvioRequest(9L, 2L, true)))
                .isInstanceOf(ItemCuestionarioNoEncontradoException.class);

        verifyNoInteractions(sesiones, agregados);
    }

    @Test
    void lasRespuestasDeUnaMismaSemanaComparten_hash_yLaSemanaSiguienteUsaOtro() {
        itemVigente();
        when(sesiones.insertarSiNoExiste(anyString(), eq(3L), org.mockito.ArgumentMatchers.any())).thenReturn(1);
        ArgumentCaptor<String> hashes = ArgumentCaptor.forClass(String.class);

        // miércoles, domingo 23:00 en Lima (mismo semana) y lunes 00:30 en Lima (semana nueva)
        for (String instante : new String[] {"2026-10-14T15:00:00Z", "2026-10-19T04:00:00Z", "2026-10-19T05:30:00Z"}) {
            Clock reloj = Clock.fixed(Instant.parse(instante), ZoneOffset.UTC);
            new CheckInRegistroService(items, sesiones, agregados, SECRETO, ZONA, reloj)
                    .registrar(7L, 5L, new CheckInEnvioRequest(9L, 2L, true));
        }

        verify(sesiones, org.mockito.Mockito.times(3)).insertarSiNoExiste(hashes.capture(), eq(3L), org.mockito.ArgumentMatchers.any());
        assertThat(hashes.getAllValues().get(0)).isEqualTo(hashes.getAllValues().get(1));
        assertThat(hashes.getAllValues().get(2)).isNotEqualTo(hashes.getAllValues().get(0));
    }
}

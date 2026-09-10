package com.kala.backend.service;

import com.kala.backend.dto.CheckInRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckInServiceTest {

    @Test
    void debeAgregarCheckInsPorEquipoYDia() {
        CheckInService service = new CheckInService();

        service.registrar(new CheckInRequest(1L, true));
        service.registrar(new CheckInRequest(1L, false));

        var agregado = service.buscarAgregadoDeHoy(1L);

        assertThat(agregado).isNotNull();
        assertThat(agregado.getTotalRespuestas()).isEqualTo(2);
        assertThat(agregado.getRespuestasSi()).isEqualTo(1);
    }

    @Test
    void debeLanzarExcepcionSiEquipoIdEsNulo() {
        CheckInService service = new CheckInService();

        assertThatThrownBy(
                () -> service.registrar(new CheckInRequest(null, true))
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeDevolverNullSiNoHayCheckInsHoy() {
        CheckInService service = new CheckInService();

        var agregado = service.buscarAgregadoDeHoy(99L);

        assertThat(agregado).isNull();
    }
}
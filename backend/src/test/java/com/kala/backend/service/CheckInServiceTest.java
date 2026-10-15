package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.model.CheckIn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckInServiceTest {

    private CheckInService service;
    private EquipoService equipoService;
    private Long equipoId;

    @BeforeEach
    void setUp() {
        equipoId = 1L;
        equipoService = mock(EquipoService.class);
        service = new CheckInService(equipoService);
    }

    @Test
    void registrarDosCheckIns_agregaAlTotalYSoloUnoAlSi() {
        service.registrar(new CheckInRequest(equipoId, true));
        service.registrar(new CheckInRequest(equipoId, false));

        CheckIn agregado = service.buscarAgregadoDeHoy(equipoId);
        assertThat(agregado.getTotalRespuestas()).isEqualTo(2);
        assertThat(agregado.getRespuestasSi()).isEqualTo(1);
    }

    @Test
    void buscarAgregadoDeHoySinCheckIns_devuelveNull() {
        assertThat(service.buscarAgregadoDeHoy(equipoId)).isNull();
    }
}
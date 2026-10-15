
package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.CheckIn;
import com.kala.backend.model.Equipo;
import com.kala.backend.repository.EmpresaRepository;
import com.kala.backend.repository.EquipoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EquipoRepository equipoRepository;

    private CheckInService service;
    private Long equipoId;

    @BeforeEach
    void setUp() {
        EmpresaService empresaService =
                new EmpresaService(empresaRepository, equipoRepository);

        EquipoService equipoService =
                new EquipoService(equipoRepository, empresaService);

        service = new CheckInService(equipoService);
        equipoId = 1L;
    }

    @Test
    void registrarDosCheckIns_agregaAlTotalYSoloUnoAlSi() {
        when(equipoRepository.findById(equipoId))
                .thenReturn(Optional.of(
                        new Equipo(equipoId, "Equipo Dev", 10L)
                ));

        service.registrar(new CheckInRequest(equipoId, true));
        service.registrar(new CheckInRequest(equipoId, false));

        CheckIn agregado = service.buscarAgregadoDeHoy(equipoId);

        assertThat(agregado.getTotalRespuestas()).isEqualTo(2);
        assertThat(agregado.getRespuestasSi()).isEqualTo(1);
    }

    @Test
    void registrarSobreEquipoInexistente_lanzaEquipoNoEncontradoException() {
        when(equipoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.registrar(new CheckInRequest(999L, true)))
                .isInstanceOf(EquipoNoEncontradoException.class);
    }

    @Test
    void registrarConEquipoIdNulo_lanzaIllegalArgumentException() {
        assertThatThrownBy(() ->
                service.registrar(new CheckInRequest(null, true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buscarAgregadoDeHoySinCheckIns_devuelveNull() {
        when(equipoRepository.findById(equipoId))
                .thenReturn(Optional.of(
                        new Equipo(equipoId, "Equipo Dev", 10L)
                ));

        assertThat(service.buscarAgregadoDeHoy(equipoId)).isNull();
    }
}

package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.dto.EmpresaRequest;
import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.CheckIn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Test unitario del service: JUnit 5 + AssertJ, sin Spring.
// CheckInService recibe un EquipoService real (y este un EmpresaService real),
// construidos a mano en setUp().
class CheckInServiceTest {

    private CheckInService service;
    private Long equipoId;

    @BeforeEach
    void setUp() {
        EmpresaService empresaService = new EmpresaService();
        EquipoService equipoService = new EquipoService(empresaService);
        service = new CheckInService(equipoService);

        Long empresaId = empresaService.crear(new EmpresaRequest("ACME", "Tecnología")).getId();
        equipoId = equipoService.crear(new EquipoRequest("Equipo Dev", empresaId)).getId();
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
    void registrarSobreEquipoInexistente_lanzaEquipoNoEncontradoException() {
        assertThatThrownBy(() -> service.registrar(new CheckInRequest(999L, true)))
                .isInstanceOf(EquipoNoEncontradoException.class);
    }

    @Test
    void registrarConEquipoIdNulo_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> service.registrar(new CheckInRequest(null, true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buscarAgregadoDeHoySinCheckIns_devuelveNull() {
        assertThat(service.buscarAgregadoDeHoy(equipoId)).isNull();
    }
}

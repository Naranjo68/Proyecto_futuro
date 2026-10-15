package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.kala.backend.dto.CheckInEnvioRequest;
import com.kala.backend.repository.AgregadoItemRepository;
import com.kala.backend.repository.SesionCheckinRepository;
import com.kala.backend.service.ItemCuestionarioLector.ItemCuestionarioInfo;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Demuestra el "todo o nada": si falla la suma después de insertar la sesión, la sesión no queda guardada.
// El test corre SIN transacción propia para que el servicio abra y revierta la suya.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(CheckInRegistroService.class)
@TestPropertySource(properties = {"kala.sesion.secreto=secreto-de-prueba", "kala.ventana.zona=America/Lima"})
class CheckInRegistroTransaccionTest {

    @MockitoBean
    private ItemCuestionarioLector items;
    @MockitoBean
    private AgregadoItemRepository agregados;

    @Autowired
    private CheckInRegistroService service;
    @Autowired
    private SesionCheckinRepository sesiones;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void siFallaLaSuma_laSesionNoQuedaGuardada() {
        when(items.buscar(9L)).thenReturn(Optional.of(new ItemCuestionarioInfo(3L, 2L)));
        when(agregados.registrarRespuesta(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("fallo simulado de la suma"));
        long antes = sesiones.count();

        assertThatThrownBy(() -> service.registrar(System.nanoTime(), 5L, new CheckInEnvioRequest(9L, 2L, true)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sesiones.count()).isEqualTo(antes);
    }
}

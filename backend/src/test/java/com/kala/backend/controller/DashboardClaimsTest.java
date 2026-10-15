package com.kala.backend.controller;

import static com.kala.backend.controller.DashboardControllerTest.jwtDe;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.service.DashboardService;
import com.kala.backend.service.DashboardService.ResultadoDashboard;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// El dashboard se prueba con los claims del contrato del JWT: la empresa sale del token, no de la petición.
@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class DashboardClaimsTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 3, 9);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService service;

    @Test
    void unRrhhSoloConsultaEquiposDeSuEmpresa() throws Exception {
        when(service.consultar(5L, 1L, 10L, LUNES))
                .thenReturn(new ResultadoDashboard(10L, LUNES, "OK", 40L, List.of()));
        when(service.consultar(5L, 1L, 20L, LUNES)).thenThrow(new EquipoNoEncontradoException(20L));

        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/20").param("semana", "2026-03-09").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isNotFound());
        verify(service).consultar(5L, 1L, 20L, LUNES);
    }

    @Test
    void sinElClaimEmpresaId_devuelve403YNoConsultaNada() throws Exception {
        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09").with(jwtDe("ROLE_RRHH", null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09")
                        .with(jwtDe("ROLE_COORDINADOR_BIENESTAR", null)))
                .andExpect(status().isForbidden());

        verify(service, never()).consultar(any(), any(), any(), any());
    }
}

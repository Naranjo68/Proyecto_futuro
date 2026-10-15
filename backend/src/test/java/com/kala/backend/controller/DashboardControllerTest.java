package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.exception.SemanaInvalidaException;
import com.kala.backend.service.DashboardService;
import com.kala.backend.service.DashboardService.ItemEstimado;
import com.kala.backend.service.DashboardService.ResultadoDashboard;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

// @WebMvcTest carga solo el controller; el service es un mock. Se prueba el mapeo HTTP y las reglas de acceso.
@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class DashboardControllerTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 3, 9);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService service;

    static RequestPostProcessor jwtDe(String rol, Long empresaId) {
        return jwt().jwt(jwt -> {
            jwt.claim("roles", List.of(rol));
            jwt.claim("uid", 5L);
            if (empresaId != null) {
                jwt.claim("empresaId", empresaId);
            }
        }).authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void empleadoYAdmin_reciben403() throws Exception {
        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09")
                        .with(jwtDe("ROLE_EMPLEADO", 1L)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09")
                        .with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rrhhConDatosSuficientes_devuelve200ConLaEstimacion() throws Exception {
        when(service.consultar(5L, 1L, 10L, LUNES)).thenReturn(new ResultadoDashboard(
                10L, LUNES, "OK", 40L, List.of(new ItemEstimado(3L, "Pulso", 0.62, 0.05))));

        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09")
                        .with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipoId").value(10))
                .andExpect(jsonPath("$.semanaInicio").value("2026-03-09"))
                .andExpect(jsonPath("$.resultado").value("OK"))
                .andExpect(jsonPath("$.totalRespuestas").value(40))
                .andExpect(jsonPath("$.items[0].itemId").value(3))
                .andExpect(jsonPath("$.items[0].tema").value("Pulso"))
                .andExpect(jsonPath("$.items[0].proporcionEstimada").value(0.62))
                .andExpect(jsonPath("$.items[0].margenError95").value(0.05));
    }

    @Test
    void coordinadorConDatosInsuficientes_devuelve200ConEstimacionesNulas() throws Exception {
        when(service.consultar(5L, 1L, 10L, LUNES)).thenReturn(new ResultadoDashboard(
                10L, LUNES, "DATOS_INSUFICIENTES", 12L, List.of(new ItemEstimado(3L, "Pulso", null, null))));

        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-09")
                        .with(jwtDe("ROLE_COORDINADOR_BIENESTAR", 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("DATOS_INSUFICIENTES"))
                .andExpect(jsonPath("$.items[0].proporcionEstimada").doesNotExist())
                .andExpect(jsonPath("$.items[0].margenError95").doesNotExist());
    }

    @Test
    void equipoDeOtraEmpresa_devuelve404() throws Exception {
        when(service.consultar(eq(5L), eq(1L), eq(99L), any()))
                .thenThrow(new EquipoNoEncontradoException(99L));

        mockMvc.perform(get("/api/dashboard/99").param("semana", "2026-03-09")
                        .with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void semanaQueNoEmpiezaEnLunes_devuelve400() throws Exception {
        when(service.consultar(eq(5L), eq(1L), eq(10L), any()))
                .thenThrow(new SemanaInvalidaException(LocalDate.of(2026, 3, 10)));

        mockMvc.perform(get("/api/dashboard/10").param("semana", "2026-03-10")
                        .with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinSemanaOConUnaFechaMalFormada_devuelve400() throws Exception {
        mockMvc.perform(get("/api/dashboard/10").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/dashboard/10").param("semana", "no-es-fecha")
                        .with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isBadRequest());
    }
}

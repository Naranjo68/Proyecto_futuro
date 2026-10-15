package com.kala.backend.controller;

import static com.kala.backend.controller.CuestionarioControllerTest.jwtDe;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.model.Cuestionario;
import com.kala.backend.service.ActivacionCuestionarioService;
import com.kala.backend.service.ActivacionCuestionarioService.ResultadoActivacion;
import com.kala.backend.service.CuestionarioConsultaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Los endpoints de cuestionarios se prueban con los claims del contrato del JWT.
@WebMvcTest(CuestionarioController.class)
@AutoConfigureMockMvc
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class CuestionarioClaimsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivacionCuestionarioService activacion;

    @MockitoBean
    private CuestionarioConsultaService consulta;

    @Test
    void adminSinEmpresaId_activaEnCualquierEmpresa() throws Exception {
        when(activacion.activar(1L, 10L)).thenReturn(new ResultadoActivacion(1L, 10L, 5.49, 20.0));
        when(activacion.activar(2L, 10L)).thenReturn(new ResultadoActivacion(2L, 10L, 5.49, 20.0));

        mockMvc.perform(post("/api/empresas/1/cuestionarios/10/activar").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/empresas/2/cuestionarios/10/activar").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isOk());
    }

    @Test
    void rrhhDeLaEmpresaA_noVeLosCuestionariosDeLaB_y404() throws Exception {
        when(consulta.listarActivos(1L)).thenReturn(List.of(new Cuestionario("PULSO", "Pulso", (short) 7, null)));

        mockMvc.perform(get("/api/empresas/1/cuestionarios").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/empresas/2/cuestionarios").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/empresas/2/cuestionarios").with(jwtDe("ROLE_COORDINADOR_BIENESTAR", 1L)))
                .andExpect(status().isNotFound());

        verify(consulta, never()).listarActivos(2L);
    }

    @Test
    void personalSinEmpresaId_recibe403() throws Exception {
        mockMvc.perform(get("/api/empresas/1/cuestionarios").with(jwtDe("ROLE_RRHH", null)))
                .andExpect(status().isForbidden());

        verify(consulta, never()).listarActivos(any());
    }

    @Test
    void empleadoSinEmpresaId_recibe403() throws Exception {
        mockMvc.perform(get("/api/cuestionarios/pendientes").with(jwtDe("ROLE_EMPLEADO", null)))
                .andExpect(status().isForbidden());

        verify(consulta, never()).pendientes(any());
    }

    @Test
    void elEmpleadoRecibeLosPendientesDeLaEmpresaDelToken() throws Exception {
        when(consulta.pendientes(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/cuestionarios/pendientes").with(jwtDe("ROLE_EMPLEADO", 7L)))
                .andExpect(status().isOk());

        verify(consulta).pendientes(7L);
    }
}

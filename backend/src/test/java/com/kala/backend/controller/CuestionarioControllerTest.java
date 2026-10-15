package com.kala.backend.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.dto.CuestionarioPendienteResponse;
import com.kala.backend.dto.CuestionarioPendienteResponse.Escala;
import com.kala.backend.dto.CuestionarioPendienteResponse.ItemPendiente;
import com.kala.backend.dto.CuestionarioPendienteResponse.Opcion;
import com.kala.backend.exception.CuestionarioNoEncontradoException;
import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.exception.PresupuestoExcedidoException;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.service.ActivacionCuestionarioService;
import com.kala.backend.service.ActivacionCuestionarioService.ResultadoActivacion;
import com.kala.backend.service.CuestionarioConsultaService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

// Matriz 401 / 403 / 200 / 404 / 422 de los endpoints de cuestionarios, con los services como mocks.
@WebMvcTest(CuestionarioController.class)
@AutoConfigureMockMvc
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class CuestionarioControllerTest {

    private static final String ACTIVAR = "/api/empresas/1/cuestionarios/10/activar";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivacionCuestionarioService activacion;

    @MockitoBean
    private CuestionarioConsultaService consulta;

    static RequestPostProcessor jwtDe(String rol, Long empresaId) {
        return jwt().jwt(jwt -> {
            jwt.claim("roles", List.of(rol));
            jwt.claim("uid", 5L);
            if (empresaId != null) {
                jwt.claim("empresaId", empresaId);
            }
        }).authorities(new SimpleGrantedAuthority(rol));
    }

    private static Cuestionario pulso() {
        Cuestionario c = new Cuestionario("PULSO", "Pulso semanal", (short) 7, null);
        ReflectionTestUtils.setField(c, "id", 10L);
        return c;
    }

    @Test
    void activar_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post(ACTIVAR)).andExpect(status().isUnauthorized());
    }

    @Test
    void activar_soloAdmin_losDemasRolesRecibenForbidden() throws Exception {
        for (String rol : List.of("ROLE_RRHH", "ROLE_COORDINADOR_BIENESTAR", "ROLE_EMPLEADO")) {
            mockMvc.perform(post(ACTIVAR).with(jwtDe(rol, 1L))).andExpect(status().isForbidden());
        }
        verify(activacion, never()).activar(any(), any());
    }

    @Test
    void activar_comoAdmin_devuelve200ConElGastoMensual() throws Exception {
        when(activacion.activar(1L, 10L)).thenReturn(new ResultadoActivacion(1L, 10L, 5.49, 20.0));

        mockMvc.perform(post(ACTIVAR).with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(1))
                .andExpect(jsonPath("$.cuestionarioId").value(10))
                .andExpect(jsonPath("$.epsilonMensual").value(5.49))
                .andExpect(jsonPath("$.epsilonMaximo").value(20.0));
    }

    @Test
    void activar_empresaOCuestionarioInexistente_devuelve404() throws Exception {
        when(activacion.activar(99L, 10L)).thenThrow(new EmpresaNoEncontradaException(99L));
        when(activacion.activar(1L, 99L)).thenThrow(new CuestionarioNoEncontradoException(99L));

        mockMvc.perform(post("/api/empresas/99/cuestionarios/10/activar").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/empresas/1/cuestionarios/99/activar").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void activar_siExcedeElPresupuesto_devuelve422() throws Exception {
        when(activacion.activar(1L, 10L)).thenThrow(new PresupuestoExcedidoException(32.96, 20.0));

        mockMvc.perform(post(ACTIVAR).with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void listar_sinToken_devuelve401_yUnEmpleadoRecibe403() throws Exception {
        mockMvc.perform(get("/api/empresas/1/cuestionarios")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/empresas/1/cuestionarios").with(jwtDe("ROLE_EMPLEADO", 1L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_adminYRrhhDeLaEmpresa_devuelven200() throws Exception {
        when(consulta.listarActivos(1L)).thenReturn(List.of(pulso()));

        mockMvc.perform(get("/api/empresas/1/cuestionarios").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("PULSO"))
                .andExpect(jsonPath("$[0].cadenciaDias").value(7))
                .andExpect(jsonPath("$[0].itemsPorEnvio").doesNotExist());
        mockMvc.perform(get("/api/empresas/1/cuestionarios").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/empresas/1/cuestionarios").with(jwtDe("ROLE_COORDINADOR_BIENESTAR", 1L)))
                .andExpect(status().isOk());
    }

    @Test
    void listar_empresaInexistenteParaElAdmin_devuelve404() throws Exception {
        when(consulta.listarActivos(99L)).thenThrow(new EmpresaNoEncontradaException(99L));

        mockMvc.perform(get("/api/empresas/99/cuestionarios").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void pendientes_devuelveLosItemsConLaConfiguracionParaAleatorizar() throws Exception {
        when(consulta.pendientes(1L)).thenReturn(List.of(new CuestionarioPendienteResponse(
                10L, "PULSO", "Pulso semanal", 7, null, List.of(new ItemPendiente(
                        3L, 1, "Pulso", "¿Hoy te sentiste bien?",
                        new Escala("BINARIA", 0, 1, List.of(new Opcion(0, "No"), new Opcion(1, "Sí"))),
                        2L, "RANDOMIZED_RESPONSE", new BigDecimal("0.750"))))));

        mockMvc.perform(get("/api/cuestionarios/pendientes").with(jwtDe("ROLE_EMPLEADO", 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("PULSO"))
                .andExpect(jsonPath("$[0].items[0].itemId").value(3))
                .andExpect(jsonPath("$[0].items[0].configuracionLdpId").value(2))
                .andExpect(jsonPath("$[0].items[0].mecanismo").value("RANDOMIZED_RESPONSE"))
                .andExpect(jsonPath("$[0].items[0].probabilidadVerdad").value(0.75))
                .andExpect(jsonPath("$[0].items[0].escala.opciones[1].etiqueta").value("Sí"));
    }

    @Test
    void pendientes_sinToken401_yLosDemasRoles403() throws Exception {
        mockMvc.perform(get("/api/cuestionarios/pendientes")).andExpect(status().isUnauthorized());
        for (String rol : List.of("ROLE_ADMIN", "ROLE_RRHH", "ROLE_COORDINADOR_BIENESTAR")) {
            mockMvc.perform(get("/api/cuestionarios/pendientes").with(jwtDe(rol, 1L)))
                    .andExpect(status().isForbidden());
        }
    }
}

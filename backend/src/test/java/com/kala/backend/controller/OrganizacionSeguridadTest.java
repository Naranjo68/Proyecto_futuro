package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.dto.EquipoRequest;
import com.kala.backend.model.Empresa;
import com.kala.backend.model.Equipo;
import com.kala.backend.service.EmpresaService;
import com.kala.backend.service.EquipoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest({EmpresaController.class, EquipoController.class})
@AutoConfigureMockMvc
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class OrganizacionSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmpresaService empresaService;

    @MockitoBean
    private EquipoService equipoService;

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPuedeListarTodasLasEmpresas() throws Exception {
        when(empresaService.listar()).thenReturn(List.of(
                new Empresa(1L, "Acme", "Tecnología"),
                new Empresa(2L, "Beta", "Servicios")));

        mockMvc.perform(get("/api/empresas").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void rrhhSoloPuedeLeerSuEmpresa() throws Exception {
        when(empresaService.buscarPorId(1L))
                .thenReturn(new Empresa(1L, "Acme", "Tecnología"));

        mockMvc.perform(get("/api/empresas/1").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/api/empresas/2").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isNotFound());

        verify(empresaService, never()).buscarPorId(2L);
    }

    @Test
    void rrhhSinEmpresaIdRecibe403YNoConsultaEmpresasNiEquipos() throws Exception {
        mockMvc.perform(get("/api/empresas").with(jwtDe("ROLE_RRHH", null)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/equipos").with(jwtDe("ROLE_RRHH", null)))
                .andExpect(status().isForbidden());

        verify(empresaService, never()).listar();
        verify(equipoService, never()).listarPorEmpresa(any());
    }

    @Test
    void coordinadorNoPuedeModificarEmpresa() throws Exception {
        mockMvc.perform(post("/api/empresas")
                        .with(jwtDe("ROLE_COORDINADOR_BIENESTAR", 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ruc":"20123456789","nombre":"Acme","rubro":"Tecnología"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void rrhhSoloListaEquiposDeSuEmpresaAunqueSoliciteOtra() throws Exception {
        when(equipoService.listarPorEmpresa(1L))
                .thenReturn(List.of(new Equipo(3L, "Ventas", 1L)));

        mockMvc.perform(get("/api/equipos")
                        .param("empresaId", "2")
                        .with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].empresaId").value(1));

        verify(equipoService).listarPorEmpresa(1L);
        verify(equipoService, never()).listarPorEmpresa(2L);
    }

    @Test
    void coordinadorNoPuedeLeerEquipoDeOtraEmpresa() throws Exception {
        when(equipoService.buscarPorIdEnEmpresa(9L, 1L))
                .thenThrow(new EquipoNoEncontradoException(9L));

        mockMvc.perform(get("/api/equipos/9")
                        .with(jwtDe("ROLE_COORDINADOR_BIENESTAR", 1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rrhhNoPuedeLeerActualizarNiEliminarEquipoDeOtraEmpresa() throws Exception {
        when(equipoService.buscarPorIdEnEmpresa(9L, 1L))
                .thenThrow(new EquipoNoEncontradoException(9L));
        when(equipoService.actualizarEnEmpresa(9L,
                new EquipoRequest("Marketing", 1L), 1L))
                .thenThrow(new EquipoNoEncontradoException(9L));
        doThrow(new EquipoNoEncontradoException(9L))
                .when(equipoService).eliminarEnEmpresa(9L, 1L);

        mockMvc.perform(get("/api/equipos/9").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/equipos/9")
                        .with(jwtDe("ROLE_RRHH", 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Marketing","empresaId":1}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/equipos/9").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rrhhNoPuedeModificarEmpresasAunqueElRecursoSeaDeOtraEmpresa() throws Exception {
        mockMvc.perform(put("/api/empresas/2")
                        .with(jwtDe("ROLE_RRHH", 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ruc":"20123456789","nombre":"Beta","rubro":"Servicios"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/empresas/2").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isForbidden());

        verify(empresaService, never()).actualizar(any(), any());
        verify(empresaService, never()).eliminar(any());
    }

    @Test
    void adminSinEmpresaIdPuedeConsultarYModificarOtrasEmpresasYEquipos() throws Exception {
        when(empresaService.buscarPorId(2L))
                .thenReturn(new Empresa(2L, "Beta", "Servicios"));
        when(empresaService.actualizar(any(), any()))
                .thenReturn(new Empresa(2L, "Beta actualizada", "Servicios"));
        when(equipoService.buscarPorId(9L))
                .thenReturn(new Equipo(9L, "Soporte", 2L));
        when(equipoService.actualizar(9L,
                new EquipoRequest("Marketing", 2L)))
                .thenReturn(new Equipo(9L, "Marketing", 2L));

        mockMvc.perform(get("/api/empresas/2").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/empresas/2")
                        .with(jwtDe("ROLE_ADMIN", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ruc":"20123456789","nombre":"Beta","rubro":"Servicios"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/empresas/2").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/equipos/9").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/equipos/9")
                        .with(jwtDe("ROLE_ADMIN", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Marketing","empresaId":2}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/equipos/9").with(jwtDe("ROLE_ADMIN", null)))
                .andExpect(status().isNoContent());
    }

    @Test
    void empleadoRecibe403AlLeerEquipos() throws Exception {
        mockMvc.perform(get("/api/equipos").with(jwtDe("ROLE_EMPLEADO", 1L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rrhhPuedeCrearEquipoSoloEnSuEmpresa() throws Exception {
        when(equipoService.crear(any()))
                .thenReturn(new Equipo(4L, "Ventas", 1L));
        String body = """
                {"nombre":"Ventas","empresaId":1}
                """;

        mockMvc.perform(post("/api/equipos")
                        .with(jwtDe("ROLE_RRHH", 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/equipos")
                        .with(jwtDe("ROLE_RRHH", 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ventas","empresaId":2}
                                """))
                .andExpect(status().isNotFound());

        verify(equipoService).crear(any());
    }

    @Test
    void rrhhSoloActualizaOEliminaEquiposDeSuEmpresa() throws Exception {
        when(equipoService.actualizarEnEmpresa(any(Long.class), any(), any(Long.class)))
                .thenReturn(new Equipo(5L, "Marketing", 1L));

        mockMvc.perform(put("/api/equipos/5")
                        .with(jwtDe("ROLE_RRHH", 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Marketing","empresaId":1}
                                """))
                .andExpect(status().isOk());

        doThrow(new EquipoNoEncontradoException(8L))
                .when(equipoService).eliminarEnEmpresa(8L, 1L);

        mockMvc.perform(delete("/api/equipos/8").with(jwtDe("ROLE_RRHH", 1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void coordinadorNoPuedeCrearEquipo() throws Exception {
        mockMvc.perform(post("/api/equipos")
                        .with(jwtDe("ROLE_COORDINADOR_BIENESTAR", 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ventas","empresaId":1}
                                """))
                .andExpect(status().isForbidden());
    }

    private static RequestPostProcessor jwtDe(String rol, Long empresaId) {
        return jwt().jwt(jwt -> {
            jwt.claim("roles", List.of(rol));
            if (empresaId != null) {
                jwt.claim("empresaId", empresaId);
            }
        }).authorities(new SimpleGrantedAuthority(rol));
    }

}

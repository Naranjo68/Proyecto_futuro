package com.kala.backend.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.controller.CheckInController;
import com.kala.backend.controller.EmpresaController;
import com.kala.backend.controller.PublicoController;
import com.kala.backend.service.CheckInRegistroService;
import com.kala.backend.service.EmpresaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Reglas generales de SecurityConfig con los filtros activos. El token se simula con jwt() o con un
// decodificador simulado; la validación real de firma, vencimiento y emisor está en JwtMatrizTest.
@WebMvcTest({PublicoController.class, EmpresaController.class, CheckInController.class})
@Import(SecurityConfig.class)
class SeguridadReglasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserDetailsService usuarios;
    @MockitoBean
    private EmpresaService empresaService;
    @MockitoBean
    private CheckInRegistroService checkInService;

    @Test
    void elEndpointPublico_respondeSinCredenciales() throws Exception {
        mockMvc.perform(get("/api/publico/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("API disponible"));
    }

    @Test
    void unRecursoProtegido_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unRecursoProtegido_conUnTokenValido_devuelve200() throws Exception {
        when(empresaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/empresas").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void conUnTokenInvalido_devuelve401() throws Exception {
        when(jwtDecoder.decode("token-invalido")).thenThrow(new BadJwtException("firma inválida"));

        mockMvc.perform(get("/api/empresas").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void httpBasicYaNoSeAcepta_devuelve401() throws Exception {
        mockMvc.perform(get("/api/empresas").with(httpBasic("ana@kala.test", "clave-segura-123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unRolSinPermiso_enUnEndpointDeEmpleado_devuelve403() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RRHH")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"itemId\": 9, \"configuracionLdpId\": 2, \"respuestaRuidosa\": true }"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(checkInService);
    }

    @Test
    void lasPeticionesDeEscrituraNoPidenTokenCsrf() throws Exception {
        // Sin credenciales debe ser 401 (no autenticado), nunca 403 por CSRF.
        mockMvc.perform(post("/api/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}

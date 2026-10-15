package com.kala.backend.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Matriz 401 / 403 / 200 de las reglas generales de SecurityConfig, con los filtros de seguridad activos.
@WebMvcTest({PublicoController.class, EmpresaController.class, CheckInController.class})
@Import(SecurityConfig.class)
class SeguridadReglasTest {

    // UserDetailsService en memoria para probar HTTP Basic de punta a punta sin base de datos.
    @TestConfiguration
    static class UsuarioDePrueba {
        @Bean
        UserDetailsService userDetailsService(PasswordEncoder encoder) {
            return new InMemoryUserDetailsManager(User.withUsername("ana@kala.test")
                    .password(encoder.encode("clave-segura-123"))
                    .authorities("ROLE_RRHH")
                    .build());
        }
    }

    @Autowired
    private MockMvc mockMvc;

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
    void unRecursoProtegido_sinCredenciales_devuelve401() throws Exception {
        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unRecursoProtegido_conCredencialesValidas_devuelve200() throws Exception {
        when(empresaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/empresas").with(httpBasic("ana@kala.test", "clave-segura-123")))
                .andExpect(status().isOk());
    }

    @Test
    void conContrasenaIncorrecta_devuelve401() throws Exception {
        mockMvc.perform(get("/api/empresas").with(httpBasic("ana@kala.test", "otra-clave")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conUnUsuarioInexistente_devuelve401() throws Exception {
        mockMvc.perform(get("/api/empresas").with(httpBasic("nadie@kala.test", "clave-segura-123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void cualquierUsuarioAutenticado_accedeALasReglasGenerales() throws Exception {
        when(empresaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/api/empresas")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RRHH")
    void unRolSinPermiso_enUnEndpointDeEmpleado_devuelve403() throws Exception {
        mockMvc.perform(post("/api/checkin")
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

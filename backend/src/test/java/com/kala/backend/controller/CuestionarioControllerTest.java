package com.kala.backend.controller;

import com.kala.backend.exception.PresupuestoExcedidoException;
import com.kala.backend.service.ActivacionCuestionarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuestionarioController.class)
@Import(CuestionarioControllerTest.TestSecurityConfig.class)
class CuestionarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivacionCuestionarioService activacionService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        public JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            return http.build();
        }
    }

    @Test
    @DisplayName("Sin autenticación devuelve 401 Unauthorized")
    void sinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(post("/api/empresas/1/cuestionarios/1/activar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rol EMPLEADO intentando activar devuelve 403 Forbidden")
    void empleadoIntentandoActivarDevuelve403() throws Exception {
        mockMvc.perform(post("/api/empresas/1/cuestionarios/1/activar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLEADO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN activa cuestionario exitosamente devuelve 200 OK")
    void adminActivaDevuelve200() throws Exception {
        mockMvc.perform(post("/api/empresas/1/cuestionarios/1/activar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Si el presupuesto se excede devuelve 422 Unprocessable Entity")
    void presupuestoExcedidoDevuelve422() throws Exception {
        doThrow(new PresupuestoExcedidoException(1L, 15.0, 10.0))
                .when(activacionService).activar(1L, 1L);

        mockMvc.perform(post("/api/empresas/1/cuestionarios/1/activar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("EMPLEADO consulta cuestionarios pendientes devuelve 200 OK")
    void empleadoConsultaPendientesDevuelve200() throws Exception {
        mockMvc.perform(get("/api/cuestionarios/pendientes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLEADO"))
                                   .jwt(jwtBuilder -> jwtBuilder.claim("empresaId", 1L))))
                .andExpect(status().isOk());
    }
}

package com.kala.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RestController
@RequestMapping("/api")
class CuestionarioClaimsDummyController {

    @PostMapping("/empresas/{empresaId}/cuestionarios/{id}/activar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<Void> activar(@PathVariable Long empresaId, @PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/empresas/{empresaId}/cuestionarios/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<String> obtenerPorEmpresa(@PathVariable Long empresaId, @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long claimEmpresaId = jwt.getClaim("empresaId");
        if (claimEmpresaId != null && !claimEmpresaId.equals(empresaId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/cuestionarios/pendientes")
    @PreAuthorize("hasRole('EMPLEADO') and authentication.token.claims['empresaId'] != null")
    public ResponseEntity<String> listarPendientes() {
        return ResponseEntity.ok("OK");
    }
}

@WebMvcTest(CuestionarioClaimsDummyController.class)
@Import({CuestionarioClaimsDummyController.class, CuestionarioClaimsTest.TestSecurityConfig.class})
class CuestionarioClaimsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            return http.build();
        }
    }

    @Test
    @DisplayName("ADMIN sin empresaId activa en cualquier empresa devuelve 200 OK")
    void adminSinEmpresaIdActivaEnCualquierEmpresaDevuelve200() throws Exception {
        mockMvc.perform(post("/api/empresas/99/cuestionarios/1/activar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RRHH de empresa A no ve los cuestionarios de la empresa B devuelve 404 Not Found")
    void rrhhEmpresaANoVeCuestionariosEmpresaBDevuelve404() throws Exception {
        mockMvc.perform(get("/api/empresas/2/cuestionarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RRHH"))
                                   .jwt(builder -> builder.claim("empresaId", 1L))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Empleado sin empresaId recibe 403 Forbidden")
    void empleadoSinEmpresaIdRecibe403() throws Exception {
        mockMvc.perform(get("/api/cuestionarios/pendientes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLEADO"))))
                .andExpect(status().isForbidden());
    }
}

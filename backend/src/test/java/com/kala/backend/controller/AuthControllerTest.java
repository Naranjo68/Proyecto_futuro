package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.config.SecurityConfig;
import com.kala.backend.dto.TokenResponse;
import com.kala.backend.exception.CredencialesInvalidasException;
import com.kala.backend.security.AutenticacionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticacionService autenticacion;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private UserDetailsService usuarios;

    @Test
    void loginValido_devuelve200ConElToken_yNingunaContrasena() throws Exception {
        when(autenticacion.login(any())).thenReturn(new TokenResponse("Bearer", "token-firmado", 1800));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"ana@kala.test\", \"password\": \"clave-segura-123\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("token-firmado"))
                .andExpect(jsonPath("$.expiresInSeconds").value(1800))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("clave"))));
    }

    @Test
    void loginConCredencialesInvalidas_devuelve401() throws Exception {
        when(autenticacion.login(any())).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"ana@kala.test\", \"password\": \"otra\" }"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSinContrasena_devuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"ana@kala.test\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_devuelveLoQueDiceElToken() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(jwt().jwt(token -> token
                        .subject("ana@kala.test")
                        .claim("uid", 7L)
                        .claim("roles", List.of("ROLE_RRHH"))
                        .claim("empresaId", 9L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana@kala.test"))
                .andExpect(jsonPath("$.uid").value(7))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_RRHH"))
                .andExpect(jsonPath("$.empresaId").value(9))
                .andExpect(jsonPath("$.equipoId").doesNotExist());
    }

    @Test
    void me_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
}

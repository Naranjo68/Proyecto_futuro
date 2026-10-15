package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.CheckInDuplicadoException;
import com.kala.backend.exception.ConfiguracionLdpNoVigenteException;
import com.kala.backend.exception.ItemCuestionarioNoEncontradoException;
import com.kala.backend.service.CheckInRegistroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

// Con los filtros de seguridad activos: se prueba también quién puede enviar. El token se simula con jwt().
@WebMvcTest(CheckInController.class)
class CheckInControllerTest {

    private static final String CUERPO_VALIDO = """
            { "itemId": 9, "configuracionLdpId": 2, "respuestaRuidosa": true }
            """;

    // El slice no carga SecurityConfig: se habilita aquí @PreAuthorize para esta prueba.
    @TestConfiguration
    @EnableMethodSecurity
    static class SeguridadDeMetodo {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInRegistroService service;

    private RequestPostProcessor empleado() {
        return jwt()
                .jwt(token -> token.claim("uid", 11L).claim("equipoId", 3L))
                .authorities(new SimpleGrantedAuthority("ROLE_EMPLEADO"));
    }

    @Test
    void empleadoEnviaCheckInValido_devuelve202_yElEquipoSaleDelToken() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .with(empleado()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isAccepted());

        verify(service).registrar(eq(11L), eq(3L), any());
    }

    @Test
    void siYaRespondioEnLaVentana_devuelve409() throws Exception {
        doThrow(new CheckInDuplicadoException()).when(service).registrar(any(), any(), any());

        mockMvc.perform(post("/api/checkin")
                        .with(empleado()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void siLaConfiguracionNoEsLaVigente_devuelve422() throws Exception {
        doThrow(new ConfiguracionLdpNoVigenteException(9L)).when(service).registrar(any(), any(), any());

        mockMvc.perform(post("/api/checkin")
                        .with(empleado()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void siElItemNoExiste_devuelve404() throws Exception {
        doThrow(new ItemCuestionarioNoEncontradoException(9L)).when(service).registrar(any(), any(), any());

        mockMvc.perform(post("/api/checkin")
                        .with(empleado()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void conCuerpoIncompleto_devuelve400_yNoLlegaAlService() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .with(empleado()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void unRRHH_noPuedeEnviarCheckIns_devuelve403() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .with(jwt().jwt(token -> token.claim("uid", 12L))
                                .authorities(new SimpleGrantedAuthority("ROLE_RRHH")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void empleadoConTokenSinElClaimDelEquipo_devuelve403() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .with(jwt().jwt(token -> token.claim("uid", 11L))
                                .authorities(new SimpleGrantedAuthority("ROLE_EMPLEADO")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }
}

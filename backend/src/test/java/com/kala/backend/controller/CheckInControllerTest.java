package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.service.CheckInService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// @WebMvcTest carga solo el controller y el GlobalExceptionHandler. MockMvc simula
// el HTTP; @MockitoBean sustituye el service por un mock. Se prueba el mapeo, no la lógica.
@WebMvcTest(CheckInController.class)
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService service;

    @Test
    void registrarCheckInValido_devuelve202() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "equipoId": 1, "respuestaRuidosa": true }
                                """))
                .andExpect(status().isAccepted());

        verify(service).registrar(any());
    }

    @Test
    void registrarConBodyIncompleto_devuelve400() throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarSobreEquipoInexistente_devuelve404() throws Exception {
        doThrow(new EquipoNoEncontradoException(999L)).when(service).registrar(any());

        mockMvc.perform(post("/api/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "equipoId": 999, "respuestaRuidosa": true }
                                """))
                .andExpect(status().isNotFound());
    }
}

package com.kala.backend.controller;

import com.kala.backend.dto.CheckInRequest;
import com.kala.backend.service.CheckInService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckInController.class)
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService checkInService;

    @Test
    void debeRegistrarCheckInValido() throws Exception {

        mockMvc.perform(
                post("/api/checkin")
                        .contentType("application/json")
                        .content("""
                                {
                                    "equipoId": 1,
                                    "respuestaRuidosa": true
                                }
                                """)
        )
        .andExpect(status().isAccepted());

        verify(checkInService).registrar(any(CheckInRequest.class));
    }

    @Test
    void debeRechazarBodyVacio() throws Exception {

        mockMvc.perform(
                post("/api/checkin")
                        .contentType("application/json")
                        .content("{}")
        )
        .andExpect(status().isBadRequest());
    }
}
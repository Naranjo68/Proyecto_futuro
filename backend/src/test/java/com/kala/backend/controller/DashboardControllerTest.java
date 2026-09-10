package com.kala.backend.controller;

import com.kala.backend.model.CheckIn;
import com.kala.backend.service.CheckInService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService checkInService;

    @Test
    void debeObtenerDashboardConAgregado() throws Exception {

        CheckIn checkIn = new CheckIn(
                1L,
                10L,
                LocalDate.now()
        );

        checkIn.registrar(true);
        checkIn.registrar(false);
        checkIn.registrar(true);

        when(checkInService.buscarAgregadoDeHoy(10L))
                .thenReturn(checkIn);

        mockMvc.perform(
                get("/api/dashboard/10")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.equipoId").value(10))
        .andExpect(jsonPath("$.respuestasSi").value(2))
        .andExpect(jsonPath("$.totalRespuestas").value(3));
    }

    @Test
    void debeDevolver404SiNoExisteAgregado() throws Exception {

        when(checkInService.buscarAgregadoDeHoy(99L))
                .thenReturn(null);

        mockMvc.perform(
                get("/api/dashboard/99")
        )
        .andExpect(status().isNotFound());
    }
}
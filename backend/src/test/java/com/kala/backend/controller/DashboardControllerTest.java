package com.kala.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.CheckIn;
import com.kala.backend.service.CheckInService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// @WebMvcTest carga solo el controller y el GlobalExceptionHandler. MockMvc simula
// el HTTP; @MockitoBean sustituye el service por un mock. Se prueba el mapeo, no la lógica.
@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService service;

    @Test
    void conAgregadoDelDia_devuelve200ConLosContadores() throws Exception {
        CheckIn agregado = new CheckIn(1L, 7L, LocalDate.now());
        agregado.registrar(true);
        agregado.registrar(false);
        when(service.buscarAgregadoDeHoy(7L)).thenReturn(agregado);

        mockMvc.perform(get("/api/dashboard/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipoId").value(7))
                .andExpect(jsonPath("$.respuestasSi").value(1))
                .andExpect(jsonPath("$.totalRespuestas").value(2));
    }

    @Test
    void sinCheckInsHoy_devuelve404() throws Exception {
        when(service.buscarAgregadoDeHoy(7L)).thenReturn(null);

        mockMvc.perform(get("/api/dashboard/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void equipoInexistente_devuelve404() throws Exception {
        when(service.buscarAgregadoDeHoy(999L)).thenThrow(new EquipoNoEncontradoException(999L));

        mockMvc.perform(get("/api/dashboard/999"))
                .andExpect(status().isNotFound());
    }
}

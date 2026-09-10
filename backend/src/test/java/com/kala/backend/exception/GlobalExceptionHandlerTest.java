package com.kala.backend.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.controller.EquipoController;
import com.kala.backend.service.EquipoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Se prueba el handler a través de un controller real (EquipoController):
// se verifica el código y la forma del cuerpo ErrorResponse.
@WebMvcTest(EquipoController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipoService service;

    @Test
    void recursoNoEncontrado_devuelve404ConErrorResponse() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new EquipoNoEncontradoException(99L));

        mockMvc.perform(get("/api/equipos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("No existe un equipo con id: 99"))
                .andExpect(jsonPath("$.ruta").value("/api/equipos/99"))
                .andExpect(jsonPath("$.fecha").exists());
    }

    @Test
    void bodyInvalido_devuelve400ConMensajesDeCampo() throws Exception {
        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "", "empresaId": null }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void reglaDeNegocioViolada_devuelve400() throws Exception {
        when(service.crear(any())).thenThrow(new IllegalArgumentException("nombre es obligatorio"));

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "x", "empresaId": 1 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("nombre es obligatorio"));
    }
}

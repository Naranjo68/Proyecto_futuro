package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.exception.EquipoNoEncontradoException;
import com.kala.backend.model.Equipo;
import com.kala.backend.service.EquipoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// @WebMvcTest carga solo el controller y el GlobalExceptionHandler. MockMvc simula
// el HTTP; @MockitoBean sustituye el service por un mock. Se prueba el mapeo, no la lógica.
@WebMvcTest(EquipoController.class)
class EquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipoService service;

    @Test
    void listarConFiltroPorEmpresa_devuelve200() throws Exception {
        when(service.listarPorEmpresa(1L)).thenReturn(List.of(new Equipo(1L, "Ventas", 1L)));

        mockMvc.perform(get("/api/equipos").param("empresaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ventas"))
                .andExpect(jsonPath("$[0].empresaId").value(1));

        verify(service).listarPorEmpresa(1L);
    }

    @Test
    void buscarPorIdInexistente_devuelve404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new EquipoNoEncontradoException(99L));

        mockMvc.perform(get("/api/equipos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearEquipoValido_devuelve201ConLocation() throws Exception {
        when(service.crear(any())).thenReturn(new Equipo(7L, "Marketing", 2L));

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Marketing", "empresaId": 2 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/equipos/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void crearEquipoConNombreVacio_devuelve400() throws Exception {
        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "", "empresaId": 2 }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearEquipoConEmpresaInexistente_devuelve404() throws Exception {
        when(service.crear(any())).thenThrow(new EmpresaNoEncontradaException(999L));

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "Equipo", "empresaId": 999 }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarEquipo_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/equipos/3"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(3L);
    }

    @Test
    void eliminarEquipoInexistente_devuelve404() throws Exception {
        doThrow(new EquipoNoEncontradoException(3L)).when(service).eliminar(eq(3L));

        mockMvc.perform(delete("/api/equipos/3"))
                .andExpect(status().isNotFound());
    }
}

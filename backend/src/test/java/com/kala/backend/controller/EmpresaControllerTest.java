package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.exception.EmpresaNoEncontradaException;
import com.kala.backend.model.Empresa;
import com.kala.backend.service.EmpresaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// @WebMvcTest carga solo el controller y el GlobalExceptionHandler. MockMvc simula
// el HTTP; @MockitoBean sustituye el service por un mock. Se prueba el mapeo, no la lógica.
@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmpresaService service;

    @Test
    void listar_devuelve200ConArreglo() throws Exception {
        when(service.listar()).thenReturn(List.of(new Empresa(1L, "ACME", "Tecnología")));

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ACME"));
    }

    @Test
    void buscarPorIdInexistente_devuelve404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new EmpresaNoEncontradaException(99L));

        mockMvc.perform(get("/api/empresas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearEmpresaValida_devuelve201ConLocation() throws Exception {
        when(service.crear(any())).thenReturn(new Empresa(5L, "ACME", "Tecnología"));

        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "ACME", "rubro": "Tecnología" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/empresas/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crearEmpresaConNombreVacio_devuelve400() throws Exception {
        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "", "rubro": "Tecnología" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarEmpresa_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/empresas/3"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(3L);
    }
}

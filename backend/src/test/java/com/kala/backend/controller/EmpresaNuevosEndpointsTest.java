
package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.dto.EmpresaConEquiposRequest;
import com.kala.backend.exception.NombreDuplicadoException;
import com.kala.backend.model.Empresa;
import com.kala.backend.service.EmpresaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmpresaController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmpresaNuevosEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmpresaService empresaService;

    @Test
    void buscarConTextoVacioDevuelve400() throws Exception {
        mockMvc.perform(get("/api/empresas/buscar")
                        .param("texto", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarConTextoDevuelveEmpresas() throws Exception {
        Empresa empresa = new Empresa(1L, "Acme", "Tecnología");

        when(empresaService.buscarPorNombre("acme"))
                .thenReturn(List.of(empresa));

        mockMvc.perform(get("/api/empresas/buscar")
                        .param("texto", "acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Acme"));
    }

    @Test
    void crearConEquiposDevuelve201YLocation() throws Exception {
        Empresa empresa = new Empresa(10L, "Acme", "Tecnología");

        when(empresaService.crearConEquipos(
                any(EmpresaConEquiposRequest.class)))
                .thenReturn(empresa);

        String json = """
                {
                    "ruc": "20123456789",
                    "nombre": "Acme",
                    "rubro": "Tecnología",
                    "equipos": ["Desarrollo", "Soporte"]
                }
                """;

        mockMvc.perform(post("/api/empresas/con-equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/empresas/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Acme"));
    }

    @Test
    void crearConEquiposDuplicadosDevuelve409() throws Exception {
        when(empresaService.crearConEquipos(
                any(EmpresaConEquiposRequest.class)))
                .thenThrow(new NombreDuplicadoException("Desarrollo"));

        String json = """
                {
                    "ruc": "20123456789",
                    "nombre": "Acme",
                    "rubro": "Tecnología",
                    "equipos": ["Desarrollo", "desarrollo"]
                }
                """;

        mockMvc.perform(post("/api/empresas/con-equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }
}

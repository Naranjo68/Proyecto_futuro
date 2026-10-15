package com.kala.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@AutoConfigureMockMvc
@WebMvcTest(EmpresaController.class)
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmpresaService service;

    private Empresa crearEmpresaPrueba(
            Long id, String nombre, String rubro, String ruc) {
        Empresa empresa = new Empresa(id, nombre, rubro);
        empresa.setRuc(ruc);
        return empresa;
    }

    @Test
    void listar_devuelve200ConArreglo() throws Exception {
        when(service.listar()).thenReturn(List.of(
                crearEmpresaPrueba(1L, "ACME", "Tecnologia", "20123456789")
        ));

        mockMvc.perform(get("/api/empresas").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ACME"))
                .andExpect(jsonPath("$[0].ruc").value("20123456789"));
    }

    @Test
    void buscarPorIdInexistente_devuelve404() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new EmpresaNoEncontradaException(99L));

        mockMvc.perform(get("/api/empresas/99").with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearEmpresaValida_devuelve201ConLocation() throws Exception {
        when(service.crear(any())).thenReturn(
                crearEmpresaPrueba(5L, "ACME", "Tecnologia", "20123456789")
        );

        mockMvc.perform(post("/api/empresas").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "ruc": "20123456789",
                                    "nombre": "ACME",
                                    "rubro": "Tecnologia"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/empresas/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.ruc").value("20123456789"))
                .andExpect(jsonPath("$.nombre").value("ACME"));
    }

    @Test
    void crearEmpresaConNombreVacio_devuelve400() throws Exception {
        mockMvc.perform(post("/api/empresas").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "ruc": "20123456789",
                                    "nombre": "",
                                    "rubro": "Tecnologia"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarEmpresa_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/empresas/3").with(admin()))
                .andExpect(status().isNoContent());

        verify(service).eliminar(3L);
    }

    private static RequestPostProcessor admin() {
        return jwt().jwt(jwt -> jwt.claim("roles", List.of("ROLE_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
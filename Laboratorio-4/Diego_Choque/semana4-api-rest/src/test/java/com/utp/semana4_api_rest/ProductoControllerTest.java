package com.utp.semana4_api_rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeListarProductos() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void debeFiltrarPorCategoria() throws Exception {
        mockMvc.perform(get("/api/productos").param("categoria", "Tecnologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Tecnologia"));
    }

    @Test
    void debeBuscarProductoPorId() throws Exception {
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").exists());
    }

    @Test
    void debeRetornar404CuandoProductoNoExiste() throws Exception {
        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe un producto con id: 999"))
                .andExpect(jsonPath("$.ruta").value("/api/productos/999"));
    }

    @Test
    void debeCrearProducto() throws Exception {
        String json = """
                {
                    "nombre": "Tablet Xiaomi",
                    "categoria": "Tecnologia",
                    "precio": 1200.0,
                    "stock": 7
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Tablet Xiaomi"))
                .andExpect(jsonPath("$.categoria").value("Tecnologia"))
                .andExpect(jsonPath("$.precio").value(1200.0))
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void debeRetornar400AlCrearConDatosInvalidos() throws Exception {
        String json = """
                {
                    "nombre": "",
                    "categoria": "",
                    "precio": -10.0,
                    "stock": -5
                }
                """;

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void debeActualizarProducto() throws Exception {
        String json = """
                {
                    "nombre": "Laptop Lenovo ThinkPad",
                    "categoria": "Tecnologia",
                    "precio": 3899.90,
                    "stock": 12
                }
                """;

        mockMvc.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop Lenovo ThinkPad"))
                .andExpect(jsonPath("$.precio").value(3899.90));
    }

    @Test
    void debeActualizarStock() throws Exception {
        String json = """
                {
                    "stock": 20
                }
                """;

        mockMvc.perform(patch("/api/productos/2/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    void debeEliminarProducto() throws Exception {
        mockMvc.perform(delete("/api/productos/3"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/productos/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void debeBuscarPorTexto() throws Exception {
        mockMvc.perform(get("/api/productos/buscar").param("texto", "Lenovo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").exists());
    }

    @Test
    void debeDisminuirStock() throws Exception {
        // Inicialmente creamos un producto para asegurar stock conocido
        String jsonCrear = """
                {
                    "nombre": "Teclado Mecanico",
                    "categoria": "Tecnologia",
                    "precio": 250.0,
                    "stock": 15
                }
                """;

        String res = mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCrear))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extraer id
        Long id = com.jayway.jsonpath.JsonPath.parse(res).read("$.id", Long.class);

        String jsonDisminuir = """
                {
                    "cantidad": 5
                }
                """;

        mockMvc.perform(patch("/api/productos/" + id + "/disminuir-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDisminuir))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void debeRetornar400AlDisminuirStockMayorAlDisponible() throws Exception {
        String jsonDisminuir = """
                {
                    "cantidad": 99999
                }
                """;

        mockMvc.perform(patch("/api/productos/1/disminuir-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDisminuir))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").exists());
    }
}

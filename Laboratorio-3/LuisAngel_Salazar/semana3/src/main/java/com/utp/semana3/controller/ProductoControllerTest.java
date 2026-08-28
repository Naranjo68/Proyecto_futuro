package main.java.com.utp.semana3.controller;

import com.utp.semana3.model.Producto;
import com.utp.semana3.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;


    // GET /productos

    @Test
    void listar_debeRetornarProductosEnJson()
            throws Exception {

        when(productoService.listar())
                .thenReturn(List.of(
                        new Producto(
                                1L,
                                "Laptop",
                                3500.00,
                                10
                        ),

                        new Producto(
                                2L,
                                "Mouse",
                                80.00,
                                20
                        )
                ));

        mockMvc.perform(
                get("/productos")
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].nombre")
                                .value("Laptop")
                )
                .andExpect(
                        jsonPath("$[1].nombre")
                                .value("Mouse")
                );
    }


    // POST /productos

    @Test
    void registrar_debeRetornarProductoCreado()
            throws Exception {

        Producto productoRegistrado =
                new Producto(
                        1L,
                        "Laptop",
                        3500.00,
                        10
                );

        when(
                productoService.registrar(
                        any(Producto.class)
                )
        ).thenReturn(productoRegistrado);

        String json = """
                {
                    "nombre": "Laptop",
                    "precio": 3500.00,
                    "stock": 10
                }
                """;

        mockMvc.perform(
                post("/productos")
                        .contentType(APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.nombre")
                                .value("Laptop")
                );
    }


    // GET /productos/{id}

    @Test
    void buscarPorIdCuandoExiste_debeRetornar200()
            throws Exception {

        Producto producto =
                new Producto(
                        1L,
                        "Laptop",
                        3500,
                        10
                );

        when(productoService.buscarPorId(1L))
                .thenReturn(Optional.of(producto));

        mockMvc.perform(
                get("/productos/1")
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.nombre")
                                .value("Laptop")
                );
    }


    @Test
    void buscarPorIdCuandoNoExiste_debeRetornar404()
            throws Exception {

        when(productoService.buscarPorId(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                get("/productos/99")
        )
                .andExpect(status().isNotFound());
    }


    // DELETE /productos/{id}

    @Test
    void eliminarCuandoExiste_debeRetornar204()
            throws Exception {

        when(productoService.eliminar(1L))
                .thenReturn(true);

        mockMvc.perform(
                delete("/productos/1")
        )
                .andExpect(status().isNoContent());
    }


    @Test
    void eliminarCuandoNoExiste_debeRetornar404()
            throws Exception {

        when(productoService.eliminar(99L))
                .thenReturn(false);

        mockMvc.perform(
                delete("/productos/99")
        )
                .andExpect(status().isNotFound());
    }


    // PUT /productos/{id}

    @Test
    void actualizarCuandoExiste_debeRetornar200()
            throws Exception {

        Producto actualizado =
                new Producto(
                        1L,
                        "Laptop Gamer",
                        4500,
                        5
                );

        when(
                productoService.actualizar(
                        eq(1L),
                        any(Producto.class)
                )
        ).thenReturn(Optional.of(actualizado));

        String json = """
                {
                    "nombre": "Laptop Gamer",
                    "precio": 4500,
                    "stock": 5
                }
                """;

        mockMvc.perform(
                put("/productos/1")
                        .contentType(APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.nombre")
                                .value("Laptop Gamer")
                )
                .andExpect(
                        jsonPath("$.precio")
                                .value(4500)
                );
    }


    @Test
    void actualizarCuandoNoExiste_debeRetornar404()
            throws Exception {

        when(
                productoService.actualizar(
                        eq(99L),
                        any(Producto.class)
                )
        ).thenReturn(Optional.empty());

        String json = """
                {
                    "nombre": "Monitor",
                    "precio": 800,
                    "stock": 5
                }
                """;

        mockMvc.perform(
                put("/productos/99")
                        .contentType(APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isNotFound());
    }
}
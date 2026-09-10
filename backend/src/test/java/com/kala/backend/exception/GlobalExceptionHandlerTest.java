package com.kala.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {
        
        @PostMapping("/test/valid")
        public void testValid(@Valid @RequestBody DummyDto dto) {}

        @GetMapping("/test/illegal")
        public void testIllegal() {
            throw new IllegalArgumentException("Argumento invalido de prueba");
        }

        @GetMapping("/test/status")
        public void testStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado");
        }
    }

    static record DummyDto(@NotBlank(message = "no debe estar vacio") String nombre) {}

    @Test
    void cuandoValidacionFalla_debeRetornar400YErrorResponse() throws Exception {
        mockMvc.perform(post("/test/valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("nombre: no debe estar vacio"))
                .andExpect(jsonPath("$.ruta").value("/test/valid"));
    }

    @Test
    void cuandoIllegalArgument_debeRetornar400YErrorResponse() throws Exception {
        mockMvc.perform(get("/test/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("Argumento invalido de prueba"));
    }

    @Test
    void cuandoResponseStatusException_debeRetornarCodigoCorrespondiente() throws Exception {
        mockMvc.perform(get("/test/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }
}
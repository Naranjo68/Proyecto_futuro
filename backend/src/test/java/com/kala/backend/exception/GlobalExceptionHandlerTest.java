package com.kala.backend.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.controller.EquipoController;
import com.kala.backend.controller.OrganizacionTestSecurityConfig;
import com.kala.backend.service.EquipoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

// Se prueba el handler a través de un controller real (EquipoController):
// se verifica el código y la forma del cuerpo ErrorResponse.
@AutoConfigureMockMvc
@WebMvcTest(EquipoController.class)
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipoService service;

    @Test
    void recursoNoEncontrado_devuelve404ConErrorResponse() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new EquipoNoEncontradoException(99L));

        mockMvc.perform(get("/api/equipos/99").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("No existe un equipo con id: 99"))
                .andExpect(jsonPath("$.ruta").value("/api/equipos/99"))
                .andExpect(jsonPath("$.fecha").exists());
    }

    @Test
    void bodyInvalido_devuelve400ConMensajesDeCampo() throws Exception {
        mockMvc.perform(post("/api/equipos").with(admin())
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

        mockMvc.perform(post("/api/equipos").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nombre": "x", "empresaId": 1 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("nombre es obligatorio"));
    }

    private static RequestPostProcessor admin() {
        return jwt().jwt(jwt -> jwt.claim("roles", List.of("ROLE_ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}

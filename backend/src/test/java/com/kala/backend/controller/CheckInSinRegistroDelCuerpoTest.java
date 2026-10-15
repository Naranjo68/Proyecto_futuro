package com.kala.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.service.CheckInRegistroService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Privacidad: ningun log registra el cuerpo de POST /api/checkin (la respuesta ruidosa y los ids que la acompanan).
// Se prueba con el nivel de log al maximo (TRACE) y con un valor centinela en el cuerpo.
@ExtendWith(OutputCaptureExtension.class)
@WebMvcTest(CheckInController.class)
@AutoConfigureMockMvc
@Import(OrganizacionTestSecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "spring.mvc.log-request-details=true"
})
class CheckInSinRegistroDelCuerpoTest {

    private static final String CENTINELA_ITEM = "987654321";
    private static final String CENTINELA_CONFIG = "123456789";

    private final LoggingSystem logging = LoggingSystem.get(getClass().getClassLoader());

    @Autowired
    private MockMvc mockMvc;

    // El nivel TRACE solo vale para esta prueba: se restaura para no ensuciar ni ralentizar el resto de la suite.
    @BeforeEach
    void subirElNivelDeLog() {
        logging.setLogLevel("ROOT", LogLevel.TRACE);
    }

    @AfterEach
    void restaurarElNivelDeLog() {
        logging.setLogLevel("ROOT", LogLevel.INFO);
    }

    @MockitoBean
    private CheckInRegistroService service;

    @Test
    void elCuerpoDelCheckInNoApareceEnNingunLog(CapturedOutput salida) throws Exception {
        mockMvc.perform(post("/api/checkin")
                        .with(jwt().jwt(j -> j.claim("roles", List.of("ROLE_EMPLEADO")).claim("uid", 5L).claim("equipoId", 7L))
                                .authorities(new SimpleGrantedAuthority("ROLE_EMPLEADO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":" + CENTINELA_ITEM + ",\"configuracionLdpId\":" + CENTINELA_CONFIG
                                + ",\"respuestaRuidosa\":true}"))
                .andExpect(status().isAccepted());

        assertThat(salida.getAll())
                .doesNotContain(CENTINELA_ITEM)
                .doesNotContain(CENTINELA_CONFIG)
                // El nombre del campo puede aparecer (lo menciona el validador); su valor, nunca.
                .doesNotContain("respuestaRuidosa=")
                .doesNotContain("respuestaRuidosa\":");
    }

    @Test
    void elCodigoDelCheckInNoUsaNingunLogger() throws IOException {
        Path base = Path.of("src/main/java/com/kala/backend");
        for (String archivo : List.of("controller/CheckInController.java", "service/CheckInRegistroService.java",
                "dto/CheckInEnvioRequest.java")) {
            String codigo = Files.readString(base.resolve(archivo));
            assertThat(codigo).as(archivo)
                    .doesNotContain("Logger")
                    .doesNotContain("@Slf4j")
                    .doesNotContain("System.out")
                    .doesNotContain("printStackTrace");
        }
    }
}

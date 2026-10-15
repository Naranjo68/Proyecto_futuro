package com.kala.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.kala.backend.service.RandomizedResponseReferencia;
import com.kala.backend.service.VentanaSemanal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

// El corte vertical del pulso de punta a punta con la aplicacion completa y la rama de pruebas de Neon:
// login por rol -> activar el cuestionario -> el dispositivo aleatoriza y envia -> dashboard con estimacion y auditoria.
// Todo corre dentro de una transaccion que se revierte al terminar, asi que no deja datos en la base.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "kala.sesion.secreto=secreto-de-prueba",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
@Transactional
class FlujoCompletoTest {

    private static final String CONTRASENA = "contrasena-de-prueba-123";
    private static final double P = 0.75;
    private static final double PI_REAL = 0.30;
    private static final int EMPLEADOS = 300;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder codificador;

    private Long usuario(String email) {
        return jdbc.queryForObject(
                "INSERT INTO usuario (email, password_hash, nombre) VALUES (?, ?, 'Persona de prueba') RETURNING id",
                Long.class, email, codificador.encode(CONTRASENA));
    }

    private String login(String email) throws Exception {
        String cuerpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + email + "\",\"password\":\"" + CONTRASENA + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(cuerpo).doesNotContain(CONTRASENA);
        return JsonPath.read(cuerpo, "$.accessToken");
    }

    // Decodifica el payload del JWT (sin verificar la firma: eso ya lo hace el servidor) para inspeccionar los claims.
    private static String payload(String token) {
        return new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
    }

    private static RequestPostProcessor bearer(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    // Un empleado "simulado" para el envio masivo: JWT valido para la seguridad, sin fila de usuario (el check-in
    // solo usa el uid para calcular el hash de sesion y no tiene FK a usuario).
    private static RequestPostProcessor empleadoSimulado(long uid, long equipoId) {
        return jwt().jwt(j -> j.claim("roles", List.of("ROLE_EMPLEADO")).claim("uid", uid).claim("equipoId", equipoId))
                .authorities(new SimpleGrantedAuthority("ROLE_EMPLEADO"));
    }

    private String checkin(Long itemId, Long configId, boolean ruidosa) {
        return "{\"itemId\":" + itemId + ",\"configuracionLdpId\":" + configId + ",\"respuestaRuidosa\":" + ruidosa + "}";
    }

    @Test
    void elPulsoDePuntaAPunta_conLoginPorRol_checkinMasivoYAuditoria() throws Exception {
        // --- Datos: una empresa con un equipo, un admin, un RRHH y un empleado.
        Long empresaId = FilasPadre.empresa(jdbc);
        Long equipoId = FilasPadre.equipo(jdbc, empresaId);
        String sufijo = java.util.UUID.randomUUID().toString();
        String emailAdmin = "admin-" + sufijo + "@kala.test";
        String emailRrhh = "rrhh-" + sufijo + "@kala.test";
        String emailEmpleado = "empleado-" + sufijo + "@kala.test";
        jdbc.update("INSERT INTO administrador_plataforma (usuario_id) VALUES (?)", usuario(emailAdmin));
        jdbc.update("INSERT INTO personal_empresa (usuario_id, empresa_id, rol) VALUES (?, ?, 'RRHH')",
                usuario(emailRrhh), empresaId);
        jdbc.update("INSERT INTO empleado_equipo (usuario_id, equipo_id) VALUES (?, ?)",
                usuario(emailEmpleado), equipoId);
        Long cuestionarioId = jdbc.queryForObject("SELECT id FROM cuestionario WHERE codigo = 'PULSO'", Long.class);

        // --- Login por rol y claims del token (sin mostrar la clave de firma).
        String tokenAdmin = login(emailAdmin);
        String tokenRrhh = login(emailRrhh);
        String tokenEmpleado = login(emailEmpleado);

        assertThat((List<String>) JsonPath.read(payload(tokenAdmin), "$.roles")).containsExactly("ROLE_ADMIN");
        assertThat(payload(tokenAdmin)).doesNotContain("empresaId").doesNotContain("equipoId");
        assertThat(((Number) JsonPath.read(payload(tokenRrhh), "$.empresaId")).longValue()).isEqualTo(empresaId);
        assertThat(((Number) JsonPath.read(payload(tokenEmpleado), "$.equipoId")).longValue()).isEqualTo(equipoId);
        assertThat(((Number) JsonPath.read(payload(tokenEmpleado), "$.empresaId")).longValue()).isEqualTo(empresaId);
        assertThat(JsonPath.<String>read(payload(tokenEmpleado), "$.sub")).isEqualTo(emailEmpleado);

        // --- Antes de activar nada, el empleado no tiene cuestionarios pendientes.
        mockMvc.perform(get("/api/cuestionarios/pendientes").with(bearer(tokenEmpleado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // --- Solo el ADMIN activa; el presupuesto del pulso semanal (~5.49) cabe en el maximo (20).
        mockMvc.perform(post("/api/empresas/" + empresaId + "/cuestionarios/" + cuestionarioId + "/activar")
                        .with(bearer(tokenRrhh)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/empresas/" + empresaId + "/cuestionarios/" + cuestionarioId + "/activar")
                        .with(bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.epsilonMensual").value(org.hamcrest.Matchers.closeTo(5.49, 0.01)))
                .andExpect(jsonPath("$.epsilonMaximo").value(20.0));

        // --- El dispositivo recibe lo necesario para aleatorizar.
        String pendientes = mockMvc.perform(get("/api/cuestionarios/pendientes").with(bearer(tokenEmpleado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("PULSO"))
                .andExpect(jsonPath("$[0].items[0].mecanismo").value("RANDOMIZED_RESPONSE"))
                .andExpect(jsonPath("$[0].items[0].probabilidadVerdad").value(0.75))
                .andReturn().getResponse().getContentAsString();
        Long itemId = ((Number) JsonPath.read(pendientes, "$[0].items[0].itemId")).longValue();
        Long configId = ((Number) JsonPath.read(pendientes, "$[0].items[0].configuracionLdpId")).longValue();

        // --- Una configuracion que ya no es la vigente se rechaza (422) y NO consume la semana de la persona.
        mockMvc.perform(post("/api/checkin").with(bearer(tokenEmpleado)).contentType(MediaType.APPLICATION_JSON)
                        .content(checkin(itemId, configId + 999_999, true)))
                .andExpect(status().isUnprocessableContent());
        mockMvc.perform(post("/api/checkin").with(bearer(tokenEmpleado)).contentType(MediaType.APPLICATION_JSON)
                        .content(checkin(itemId + 999_999, configId, true)))
                .andExpect(status().isNotFound());

        // --- El primer envio se acepta y el segundo de la misma semana es un conflicto.
        mockMvc.perform(post("/api/checkin").with(bearer(tokenEmpleado)).contentType(MediaType.APPLICATION_JSON)
                        .content(checkin(itemId, configId, true)))
                .andExpect(status().isAccepted());
        mockMvc.perform(post("/api/checkin").with(bearer(tokenEmpleado)).contentType(MediaType.APPLICATION_JSON)
                        .content(checkin(itemId, configId, false)))
                .andExpect(status().isConflict());

        // --- Con un solo envio hay datos insuficientes: sin proporcion, y la consulta queda auditada.
        LocalDate semana = VentanaSemanal.inicioDeVentana(LocalDate.now(ZoneId.of("America/Lima")));
        mockMvc.perform(get("/api/dashboard/" + equipoId).param("semana", semana.toString()).with(bearer(tokenRrhh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("DATOS_INSUFICIENTES"))
                .andExpect(jsonPath("$.items[0].proporcionEstimada").doesNotExist())
                .andExpect(jsonPath("$.items[0].margenError95").doesNotExist());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM consulta_dashboard WHERE equipo_id = ?", Long.class, equipoId))
                .isEqualTo(1L);

        // --- 300 empleados (pi real = 0.30) aleatorizan en su "dispositivo" con Randomized Response y envian.
        Random azar = new Random(42);
        for (int i = 0; i < EMPLEADOS; i++) {
            boolean real = azar.nextDouble() < PI_REAL;
            boolean ruidosa = RandomizedResponseReferencia.perturbar(real, P, azar);
            mockMvc.perform(post("/api/checkin").with(empleadoSimulado(1_000_000L + i, equipoId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkin(itemId, configId, ruidosa)))
                    .andExpect(status().isAccepted());
        }

        // --- Con suficientes datos el dashboard estima la proporcion real a partir de respuestas ruidosas.
        String dashboard = mockMvc.perform(get("/api/dashboard/" + equipoId).param("semana", semana.toString())
                        .with(bearer(tokenRrhh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("OK"))
                .andExpect(jsonPath("$.totalRespuestas").value(EMPLEADOS + 1))
                .andReturn().getResponse().getContentAsString();
        double estimada = ((Number) JsonPath.read(dashboard, "$.items[0].proporcionEstimada")).doubleValue();
        double margen = ((Number) JsonPath.read(dashboard, "$.items[0].margenError95")).doubleValue();
        assertThat(margen).isPositive();
        assertThat(Math.abs(estimada - PI_REAL)).as("la estimacion (%s) debe acercarse a 0.30", estimada)
                .isLessThanOrEqualTo(margen * 1.5);
        assertThat(dashboard).doesNotContain(emailEmpleado).doesNotContain("usuario");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM consulta_dashboard WHERE equipo_id = ?", Long.class, equipoId))
                .isEqualTo(2L);

        // --- Reglas de acceso del dashboard: ni el ADMIN ni el empleado lo ven; la semana debe empezar en lunes.
        mockMvc.perform(get("/api/dashboard/" + equipoId).param("semana", semana.toString()).with(bearer(tokenAdmin)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/" + equipoId).param("semana", semana.toString()).with(bearer(tokenEmpleado)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/" + equipoId).param("semana", semana.plusDays(1).toString())
                        .with(bearer(tokenRrhh)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/dashboard/" + (equipoId + 999_999)).param("semana", semana.toString())
                        .with(bearer(tokenRrhh)))
                .andExpect(status().isNotFound());

        // --- Privacidad: lo guardado de los check-ins es solo un hash, un cuestionario y una fecha.
        assertThat(jdbc.queryForObject("SELECT count(*) FROM sesion_checkin WHERE cuestionario_id = ?", Long.class, cuestionarioId))
                .isGreaterThanOrEqualTo((long) EMPLEADOS + 1);
        assertThat(jdbc.queryForObject(
                "SELECT total_respuestas FROM agregado_item WHERE equipo_id = ? AND item_id = ?",
                Integer.class, equipoId, itemId)).isEqualTo(EMPLEADOS + 1);
    }

    @Test
    void sinTokenOConTokenAlterado_nadaSePuedeConsultar() throws Exception {
        mockMvc.perform(get("/api/cuestionarios/pendientes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/dashboard/1").param("semana", "2026-03-09")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/checkin").contentType(MediaType.APPLICATION_JSON).content(checkin(1L, 1L, true)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/dashboard/1").param("semana", "2026-03-09")
                        .with(bearer("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4In0.firma-falsa")))
                .andExpect(status().isUnauthorized());
    }
}

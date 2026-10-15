package com.kala.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kala.backend.controller.AdminReporteController;
import com.kala.backend.controller.AuthController;
import com.kala.backend.controller.PublicoController;
import com.kala.backend.repository.AdministradorPlataformaRepository;
import com.kala.backend.repository.EmpleadoEquipoRepository;
import com.kala.backend.repository.PersonalEmpresaRepository;
import com.kala.backend.repository.UsuarioRepository;
import com.kala.backend.security.AutenticacionService;
import com.kala.backend.security.IdentidadLector.Identidad;
import com.kala.backend.security.TokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// Matriz de 10 casos con el decodificador REAL (firma HS256, vencimiento y emisor), sin base de datos.
// Los tokens se emiten con el TokenService real.
@WebMvcTest({AuthController.class, AdminReporteController.class, PublicoController.class})
@Import({SecurityConfig.class, JwtConfig.class, TokenService.class})
@TestPropertySource(properties = {
        "app.jwt.issuer=kala-api",
        "app.jwt.ttl-minutes=30",
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "kala.ventana.zona=America/Lima"
})
class JwtMatrizTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokens;
    @Autowired
    private JwtEncoder encoder;

    @MockitoBean
    private AutenticacionService autenticacion;
    @MockitoBean
    private UserDetailsService usuarios;
    @MockitoBean
    private UsuarioRepository usuarioRepo;
    @MockitoBean
    private EmpleadoEquipoRepository empleadoRepo;
    @MockitoBean
    private PersonalEmpresaRepository personalRepo;
    @MockitoBean
    private AdministradorPlataformaRepository adminRepo;

    private String tokenDe(String rol, Long empresaId, Long equipoId) {
        return tokens.crearToken(
                UsernamePasswordAuthenticationToken.authenticated("ana@kala.test", null, List.of(new SimpleGrantedAuthority(rol))),
                new Identidad(7L, empresaId, equipoId));
    }

    private String tokenCon(JwtEncoder codificador, String emisor, Instant emitido, Instant vence) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(emisor).subject("ana@kala.test")
                .issuedAt(emitido).expiresAt(vence).claim("roles", List.of("ROLE_ADMIN")).build();
        return codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void caso01_elEndpointPublico_respondeSinToken() throws Exception {
        mockMvc.perform(get("/api/publico/estado")).andExpect(status().isOk());
    }

    @Test
    void caso02_unAdminConTokenValido_veElReporte() throws Exception {
        mockMvc.perform(get("/api/admin/reporte").header("Authorization", bearer(tokenDe("ROLE_ADMIN", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.totalUsuarios").value(0));
    }

    @Test
    void caso03_unEmpleadoConTokenValido_noVeElReporte_403() throws Exception {
        mockMvc.perform(get("/api/admin/reporte").header("Authorization", bearer(tokenDe("ROLE_EMPLEADO", 9L, 3L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void caso04_unRRHHConTokenValido_noVeElReporte_403() throws Exception {
        mockMvc.perform(get("/api/admin/reporte").header("Authorization", bearer(tokenDe("ROLE_RRHH", 9L, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void caso05_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/admin/reporte")).andExpect(status().isUnauthorized());
    }

    @Test
    void caso06_meDevuelveLosClaimsDeUnTokenEmitidoPorElServicio() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(tokenDe("ROLE_EMPLEADO", 9L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana@kala.test"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_EMPLEADO"))
                .andExpect(jsonPath("$.empresaId").value(9))
                .andExpect(jsonPath("$.equipoId").value(3));
    }

    @Test
    void caso07_unTokenConLaFirmaAlterada_devuelve401() throws Exception {
        String token = tokenDe("ROLE_ADMIN", null, null);
        String[] partes = token.split("\\.");
        char original = partes[2].charAt(10);
        String firmaAlterada = partes[2].substring(0, 10) + (original == 'A' ? 'B' : 'A') + partes[2].substring(11);

        mockMvc.perform(get("/api/admin/reporte")
                        .header("Authorization", bearer(partes[0] + "." + partes[1] + "." + firmaAlterada)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void caso08_unTokenVencido_devuelve401() throws Exception {
        String vencido = tokenCon(encoder, "kala-api", Instant.now().minusSeconds(3600), Instant.now().minusSeconds(600));

        mockMvc.perform(get("/api/admin/reporte").header("Authorization", bearer(vencido)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void caso09_unTokenDeOtroEmisor_devuelve401() throws Exception {
        String ajeno = tokenCon(encoder, "otro-emisor", Instant.now(), Instant.now().plusSeconds(600));

        mockMvc.perform(get("/api/admin/reporte").header("Authorization", bearer(ajeno)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void caso10_unTokenFirmadoConOtraClave_devuelve401() throws Exception {
        JwtEncoder otraClave = new NimbusJwtEncoder(new ImmutableSecret<>(
                new SecretKeySpec("ABCDEFGHIJKLMNOPQRSTUVWXYZ012345".getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        String falso = tokenCon(otraClave, "kala-api", Instant.now(), Instant.now().plusSeconds(600));

        mockMvc.perform(get("/api/admin/reporte").header("Authorization", bearer(falso)))
                .andExpect(status().isUnauthorized());
    }
}

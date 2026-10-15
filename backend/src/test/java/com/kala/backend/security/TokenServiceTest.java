package com.kala.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.kala.backend.security.IdentidadLector.Identidad;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class TokenServiceTest {

    // Clave exclusiva de prueba (32 bytes). No se reutiliza en ningún otro entorno.
    private static final SecretKey CLAVE =
            new SecretKeySpec("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(CLAVE).macAlgorithm(MacAlgorithm.HS256).build();
    private final TokenService service = new TokenService(
            new NimbusJwtEncoder(new ImmutableSecret<>(CLAVE)), "kala-api", 30,
            Clock.fixed(Instant.now(), ZoneOffset.UTC));

    private Authentication autenticado(String... autoridades) {
        return UsernamePasswordAuthenticationToken.authenticated("ana@kala.test", null,
                java.util.Arrays.stream(autoridades).map(SimpleGrantedAuthority::new).toList());
    }

    @Test
    void elTokenContieneElUsuario_elEmisor_yLosRoles() {
        Jwt jwt = decoder.decode(service.crearToken(autenticado("ROLE_RRHH"), new Identidad(7L, 9L, null)));

        assertThat(jwt.getSubject()).isEqualTo("ana@kala.test");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("kala-api");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_RRHH");
        assertThat(((Number) jwt.getClaim("uid")).longValue()).isEqualTo(7L);
    }

    @Test
    void unRRHH_llevaEmpresaYNoLlevaEquipo() {
        Jwt jwt = decoder.decode(service.crearToken(autenticado("ROLE_RRHH"), new Identidad(7L, 9L, null)));

        assertThat(((Number) jwt.getClaim("empresaId")).longValue()).isEqualTo(9L);
        assertThat(jwt.getClaims()).doesNotContainKey("equipoId");
    }

    @Test
    void unEmpleado_llevaEmpresaYEquipo() {
        Jwt jwt = decoder.decode(service.crearToken(autenticado("ROLE_EMPLEADO"), new Identidad(11L, 9L, 3L)));

        assertThat(((Number) jwt.getClaim("empresaId")).longValue()).isEqualTo(9L);
        assertThat(((Number) jwt.getClaim("equipoId")).longValue()).isEqualTo(3L);
    }

    @Test
    void elAdministrador_noLlevaEmpresaNiEquipo() {
        Jwt jwt = decoder.decode(service.crearToken(autenticado("ROLE_ADMIN"), new Identidad(1L, null, null)));

        assertThat(jwt.getClaims()).doesNotContainKeys("empresaId", "equipoId");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_ADMIN");
    }

    @Test
    void soloSeIncluyenLasAutoridadesQueSonRoles() {
        Jwt jwt = decoder.decode(service.crearToken(autenticado("ROLE_EMPLEADO", "SCOPE_lectura"), new Identidad(11L, 9L, 3L)));

        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ROLE_EMPLEADO");
    }

    @Test
    void elTokenVenceALosMinutosConfigurados() {
        Jwt jwt = decoder.decode(service.crearToken(autenticado("ROLE_ADMIN"), new Identidad(1L, null, null)));

        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
        assertThat(service.expiraEnSegundos()).isEqualTo(1800);
    }

    @Test
    void elTokenNoLlevaContrasenasNiNadaAjenoAlContrato() {
        Jwt jwt = decoder.decode(service.crearToken(autenticado("ROLE_EMPLEADO"), new Identidad(11L, 9L, 3L)));

        assertThat(jwt.getClaims().keySet())
                .containsExactlyInAnyOrder("iss", "sub", "iat", "exp", "uid", "roles", "empresaId", "equipoId");
        assertThat(List.copyOf(jwt.getClaims().values()).toString()).doesNotContain("password");
    }
}

package com.kala.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

class JwtConfigTest {

    private static String base64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private final JwtConfig config = new JwtConfig();
    private final SecretKey clave = config.jwtSecretKey(base64("0123456789abcdef0123456789abcdef"));
    private final JwtEncoder encoder = config.jwtEncoder(clave);
    private final JwtDecoder decoder = config.jwtDecoder(clave, "kala-api");

    private String token(JwtEncoder codificador, String emisor, Instant emitido, Instant vence) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(emisor).subject("ana@kala.test")
                .issuedAt(emitido).expiresAt(vence).build();
        return codificador.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Test
    void unaClaveDeMenosDe32Bytes_seRechaza() {
        assertThatThrownBy(() -> config.jwtSecretKey(base64("clave-corta")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void unaClaveDe32Bytes_seAcepta_yUsaHmacSha256() {
        assertThat(clave.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(clave.getEncoded()).hasSize(32);
    }

    @Test
    void unTokenValido_seDecodifica() {
        String valido = token(encoder, "kala-api", Instant.now(), Instant.now().plusSeconds(600));

        assertThat(decoder.decode(valido).getSubject()).isEqualTo("ana@kala.test");
    }

    @Test
    void unTokenVencido_seRechaza() {
        String vencido = token(encoder, "kala-api", Instant.now().minusSeconds(3600), Instant.now().minusSeconds(600));

        assertThatThrownBy(() -> decoder.decode(vencido)).isInstanceOf(JwtException.class);
    }

    @Test
    void unTokenDeOtroEmisor_seRechaza() {
        String ajeno = token(encoder, "otro-emisor", Instant.now(), Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> decoder.decode(ajeno)).isInstanceOf(JwtException.class);
    }

    @Test
    void unTokenFirmadoConOtraClave_seRechaza() {
        JwtEncoder otraClave = config.jwtEncoder(config.jwtSecretKey(base64("ABCDEFGHIJKLMNOPQRSTUVWXYZ012345")));
        String falso = token(otraClave, "kala-api", Instant.now(), Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> decoder.decode(falso)).isInstanceOf(JwtException.class);
    }
}

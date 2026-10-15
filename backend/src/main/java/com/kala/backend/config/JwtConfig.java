package com.kala.backend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

// Emisión y verificación de los JWT (HS256). La clave sale de una variable de entorno (JWT_SECRET_BASE64):
// nunca va en el código ni en el repositorio. Si falta o es débil, la aplicación no arranca.
@Configuration
public class JwtConfig {

    private static final int BYTES_MINIMOS = 32;

    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret-base64}") String valor) {
        byte[] bytes = Base64.getDecoder().decode(valor);
        if (bytes.length < BYTES_MINIMOS) {
            throw new IllegalArgumentException("JWT: se requieren al menos " + BYTES_MINIMOS + " bytes en la clave");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey clave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    // Rechaza tokens con firma alterada, vencidos o de otro emisor.
    @Bean
    JwtDecoder jwtDecoder(SecretKey clave, @Value("${app.jwt.issuer}") String emisor) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(emisor));
        return decoder;
    }
}

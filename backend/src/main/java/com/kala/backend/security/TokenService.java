package com.kala.backend.security;

import com.kala.backend.security.IdentidadLector.Identidad;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

// Emite el JWT firmado. Recibe una identidad ya autenticada: no ve contraseñas ni consulta la base para firmar.
// Un JWT firmado NO está cifrado: nunca se ponen contraseñas ni datos sensibles en sus claims.
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final String emisor;
    private final long ttlMinutos;
    private final Clock reloj;

    @Autowired
    public TokenService(JwtEncoder encoder,
                        @Value("${app.jwt.issuer}") String emisor,
                        @Value("${app.jwt.ttl-minutes:30}") long ttlMinutos) {
        this(encoder, emisor, ttlMinutos, Clock.systemUTC());
    }

    // Constructor para pruebas, con un reloj controlable.
    TokenService(JwtEncoder encoder, String emisor, long ttlMinutos, Clock reloj) {
        this.encoder = encoder;
        this.emisor = emisor;
        this.ttlMinutos = ttlMinutos;
        this.reloj = reloj;
    }

    public long expiraEnSegundos() {
        return ttlMinutos * 60;
    }

    public String crearToken(Authentication autenticacion, Identidad identidad) {
        Instant ahora = reloj.instant();
        List<String> roles = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridad -> autoridad.startsWith("ROLE_"))
                .toList();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(emisor)
                .subject(autenticacion.getName())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(ttlMinutos, ChronoUnit.MINUTES))
                .claim("uid", identidad.uid())
                .claim("roles", roles);
        if (identidad.empresaId() != null) {
            claims.claim("empresaId", identidad.empresaId());
        }
        if (identidad.equipoId() != null) {
            claims.claim("equipoId", identidad.equipoId());
        }

        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, claims.build())).getTokenValue();
    }
}

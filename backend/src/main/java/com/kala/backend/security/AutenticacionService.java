package com.kala.backend.security;

import com.kala.backend.dto.LoginRequest;
import com.kala.backend.dto.TokenResponse;
import com.kala.backend.exception.CredencialesInvalidasException;
import com.kala.backend.security.IdentidadLector.Identidad;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

// Valida las credenciales con el AuthenticationManager (UserDetailsService + BCrypt) y emite el token.
@Service
public class AutenticacionService {

    private final AuthenticationManager manager;
    private final IdentidadLector identidades;
    private final TokenService tokens;

    public AutenticacionService(AuthenticationManager manager, IdentidadLector identidades, TokenService tokens) {
        this.manager = manager;
        this.identidades = identidades;
        this.tokens = tokens;
    }

    public TokenResponse login(LoginRequest request) {
        Authentication autenticacion;
        try {
            autenticacion = manager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (AuthenticationException e) {
            throw new CredencialesInvalidasException();
        }
        Identidad identidad = identidades.porEmail(autenticacion.getName());
        return new TokenResponse("Bearer", tokens.crearToken(autenticacion, identidad), tokens.expiraEnSegundos());
    }
}

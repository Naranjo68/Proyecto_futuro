package com.kala.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kala.backend.dto.LoginRequest;
import com.kala.backend.dto.TokenResponse;
import com.kala.backend.exception.CredencialesInvalidasException;
import com.kala.backend.security.IdentidadLector.Identidad;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AutenticacionServiceTest {

    private AuthenticationManager manager;
    private IdentidadLector identidades;
    private TokenService tokens;
    private AutenticacionService service;

    @BeforeEach
    void setUp() {
        manager = mock(AuthenticationManager.class);
        identidades = mock(IdentidadLector.class);
        tokens = mock(TokenService.class);
        service = new AutenticacionService(manager, identidades, tokens);
    }

    @Test
    void credencialesValidas_devuelvenElTokenBearerConSuVigencia() {
        Authentication autenticado = UsernamePasswordAuthenticationToken.authenticated(
                "ana@kala.test", null, List.of(new SimpleGrantedAuthority("ROLE_RRHH")));
        Identidad identidad = new Identidad(7L, 9L, null);
        when(manager.authenticate(any())).thenReturn(autenticado);
        when(identidades.porEmail("ana@kala.test")).thenReturn(identidad);
        when(tokens.crearToken(autenticado, identidad)).thenReturn("token-firmado");
        when(tokens.expiraEnSegundos()).thenReturn(1800L);

        TokenResponse respuesta = service.login(new LoginRequest("Ana@kala.test", "clave-segura-123"));

        assertThat(respuesta).isEqualTo(new TokenResponse("Bearer", "token-firmado", 1800L));
    }

    @Test
    void contrasenaIncorrecta_lanzaCredencialesInvalidas_sinEmitirToken() {
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));

        assertThatThrownBy(() -> service.login(new LoginRequest("ana@kala.test", "otra")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas");

        verifyNoInteractions(tokens, identidades);
    }

    @Test
    void cuentaDeshabilitada_seResponde_igualQueUnaContrasenaIncorrecta() {
        when(manager.authenticate(any())).thenThrow(new DisabledException("deshabilitada"));

        assertThatThrownBy(() -> service.login(new LoginRequest("ana@kala.test", "clave-segura-123")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas");
    }
}

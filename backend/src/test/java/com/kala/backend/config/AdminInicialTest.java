package com.kala.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kala.backend.model.AdministradorPlataforma;
import com.kala.backend.model.Usuario;
import com.kala.backend.repository.AdministradorPlataformaRepository;
import com.kala.backend.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AdminInicialTest {

    private static final String CORREO = "admin@kala.test";
    private static final String CLAVE = "clave-segura-123";

    private UsuarioRepository usuarios;
    private AdministradorPlataformaRepository administradores;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        administradores = mock(AdministradorPlataformaRepository.class);
    }

    private AdminInicial adminInicial(String email, String clave) {
        return new AdminInicial(usuarios, administradores, email, clave);
    }

    private Usuario usuarioConId(String email, long id) {
        Usuario usuario = new Usuario(email, "hash", "Administrador");
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    @Test
    void sinAdministradorPrevio_creaElUsuarioConHashBcryptYSuFilaDeAdministrador() {
        when(usuarios.findByEmail(CORREO)).thenReturn(Optional.empty());
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario guardado = inv.getArgument(0);
            ReflectionTestUtils.setField(guardado, "id", 7L);
            return guardado;
        });
        when(administradores.existsById(7L)).thenReturn(false);

        adminInicial(CORREO, CLAVE).crearSiHaceFalta();

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(usuarioGuardado.capture());
        String hash = usuarioGuardado.getValue().getPasswordHash();
        assertThat(hash).isNotEqualTo(CLAVE).startsWith("$2");
        assertThat(new BCryptPasswordEncoder().matches(CLAVE, hash)).isTrue();

        ArgumentCaptor<AdministradorPlataforma> fila = ArgumentCaptor.forClass(AdministradorPlataforma.class);
        verify(administradores).save(fila.capture());
        assertThat(fila.getValue().getUsuarioId()).isEqualTo(7L);
    }

    @Test
    void segundaEjecucionConElAdministradorYaCreado_noDuplicaNada() {
        when(usuarios.findByEmail(CORREO)).thenReturn(Optional.of(usuarioConId(CORREO, 7L)));
        when(administradores.existsById(7L)).thenReturn(true);

        adminInicial(CORREO, CLAVE).crearSiHaceFalta();

        verify(usuarios, never()).save(any());
        verify(administradores, never()).save(any());
    }

    @Test
    void usuarioExistenteSinFilaDeAdministrador_agregaSoloLaFila() {
        when(usuarios.findByEmail(CORREO)).thenReturn(Optional.of(usuarioConId(CORREO, 7L)));
        when(administradores.existsById(7L)).thenReturn(false);

        adminInicial(CORREO, CLAVE).crearSiHaceFalta();

        verify(usuarios, never()).save(any());
        verify(administradores).save(any(AdministradorPlataforma.class));
    }

    @Test
    void elCorreoSeNormalizaAMinusculasYSinEspacios() {
        when(usuarios.findByEmail(CORREO)).thenReturn(Optional.of(usuarioConId(CORREO, 7L)));
        when(administradores.existsById(7L)).thenReturn(true);

        adminInicial("  Admin@KALA.test ", CLAVE).crearSiHaceFalta();

        verify(usuarios).findByEmail(CORREO);
    }

    @Test
    void sinVariablesConfiguradas_noCreaNada() {
        adminInicial("", "").crearSiHaceFalta();

        verifyNoInteractions(usuarios, administradores);
    }

    @Test
    void conContrasenaCorta_noCreaNada() {
        adminInicial(CORREO, "corta").crearSiHaceFalta();

        verifyNoInteractions(usuarios, administradores);
    }
}

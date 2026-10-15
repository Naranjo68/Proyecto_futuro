package com.kala.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kala.backend.model.PersonalEmpresa;
import com.kala.backend.model.RolPersonal;
import com.kala.backend.model.Usuario;
import com.kala.backend.repository.AdministradorPlataformaRepository;
import com.kala.backend.repository.EmpleadoEquipoRepository;
import com.kala.backend.repository.PersonalEmpresaRepository;
import com.kala.backend.repository.UsuarioRepository;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

class CustomUserDetailsServiceTest {

    private static final String CORREO = "ana@kala.test";

    private UsuarioRepository usuarios;
    private EmpleadoEquipoRepository empleados;
    private PersonalEmpresaRepository personal;
    private AdministradorPlataformaRepository administradores;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        empleados = mock(EmpleadoEquipoRepository.class);
        personal = mock(PersonalEmpresaRepository.class);
        administradores = mock(AdministradorPlataformaRepository.class);
        service = new CustomUserDetailsService(usuarios, empleados, personal, administradores);
        when(personal.findById(7L)).thenReturn(Optional.empty());
    }

    private Usuario usuario(boolean activo) {
        Usuario usuario = new Usuario(CORREO, "$2a$10$hash-de-prueba", "Ana");
        ReflectionTestUtils.setField(usuario, "id", 7L);
        usuario.setActivo(activo);
        when(usuarios.findByEmail(CORREO)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    private Set<String> roles(UserDetails detalles) {
        return detalles.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @Test
    void empleadoDeUnEquipo_recibeSoloRoleEmpleado() {
        usuario(true);
        when(empleados.existsById(7L)).thenReturn(true);

        assertThat(roles(service.loadUserByUsername(CORREO))).containsExactly("ROLE_EMPLEADO");
    }

    @Test
    void personalDeEmpresa_recibeElRolDeSuSubtipo() {
        usuario(true);
        when(personal.findById(7L)).thenReturn(Optional.of(new PersonalEmpresa(7L, 9L, RolPersonal.COORDINADOR_BIENESTAR)));

        assertThat(roles(service.loadUserByUsername(CORREO))).containsExactly("ROLE_COORDINADOR_BIENESTAR");
    }

    @Test
    void administradorDeLaPlataforma_recibeRoleAdmin() {
        usuario(true);
        when(administradores.existsById(7L)).thenReturn(true);

        assertThat(roles(service.loadUserByUsername(CORREO))).containsExactly("ROLE_ADMIN");
    }

    @Test
    void unaPersonaConDosSubtipos_recibeAmbosRoles() {
        usuario(true);
        when(empleados.existsById(7L)).thenReturn(true);
        when(personal.findById(7L)).thenReturn(Optional.of(new PersonalEmpresa(7L, 9L, RolPersonal.RRHH)));

        assertThat(roles(service.loadUserByUsername(CORREO))).containsExactlyInAnyOrder("ROLE_EMPLEADO", "ROLE_RRHH");
    }

    @Test
    void usuarioSinSubtipos_quedaSinRoles() {
        usuario(true);

        assertThat(roles(service.loadUserByUsername(CORREO))).isEmpty();
    }

    @Test
    void usuarioInactivo_quedaDeshabilitado() {
        usuario(false);

        assertThat(service.loadUserByUsername(CORREO).isEnabled()).isFalse();
    }

    @Test
    void elUsuarioActivo_quedaHabilitado_yConservaSuHash() {
        usuario(true);

        UserDetails detalles = service.loadUserByUsername(CORREO);

        assertThat(detalles.isEnabled()).isTrue();
        assertThat(detalles.getPassword()).isEqualTo("$2a$10$hash-de-prueba");
    }

    @Test
    void elCorreoSeNormaliza_antesDeBuscar() {
        usuario(true);

        assertThat(service.loadUserByUsername("  Ana@KALA.test ").getUsername()).isEqualTo(CORREO);
    }

    @Test
    void usuarioInexistente_lanzaUsernameNotFound_conMensajeGenerico() {
        when(usuarios.findByEmail("nadie@kala.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nadie@kala.test"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Credenciales inválidas");
    }
}

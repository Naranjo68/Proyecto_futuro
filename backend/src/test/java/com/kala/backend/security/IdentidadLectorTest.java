package com.kala.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kala.backend.model.EmpleadoEquipo;
import com.kala.backend.model.PersonalEmpresa;
import com.kala.backend.model.RolPersonal;
import com.kala.backend.model.Usuario;
import com.kala.backend.repository.EmpleadoEquipoRepository;
import com.kala.backend.repository.PersonalEmpresaRepository;
import com.kala.backend.repository.UsuarioRepository;
import com.kala.backend.security.IdentidadLector.Identidad;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class IdentidadLectorTest {

    private static final String CORREO = "ana@kala.test";

    private UsuarioRepository usuarios;
    private EmpleadoEquipoRepository empleados;
    private PersonalEmpresaRepository personal;
    private EmpresaDeEquipoLector empresas;
    private IdentidadLector lector;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        empleados = mock(EmpleadoEquipoRepository.class);
        personal = mock(PersonalEmpresaRepository.class);
        empresas = mock(EmpresaDeEquipoLector.class);
        lector = new IdentidadLector(usuarios, empleados, personal, empresas);

        Usuario usuario = new Usuario(CORREO, "hash", "Ana");
        ReflectionTestUtils.setField(usuario, "id", 7L);
        when(usuarios.findByEmail(CORREO)).thenReturn(Optional.of(usuario));
        when(empleados.findById(7L)).thenReturn(Optional.empty());
        when(personal.findById(7L)).thenReturn(Optional.empty());
    }

    @Test
    void empleado_traeSuEquipo_yLaEmpresaSeDerivaDelEquipo() {
        when(empleados.findById(7L)).thenReturn(Optional.of(new EmpleadoEquipo(7L, 3L)));
        when(empresas.empresaDelEquipo(3L)).thenReturn(Optional.of(9L));

        assertThat(lector.porEmail(CORREO)).isEqualTo(new Identidad(7L, 9L, 3L));
    }

    @Test
    void personalDeEmpresa_traeSuEmpresa_sinEquipo() {
        when(personal.findById(7L)).thenReturn(Optional.of(new PersonalEmpresa(7L, 9L, RolPersonal.RRHH)));

        assertThat(lector.porEmail(CORREO)).isEqualTo(new Identidad(7L, 9L, null));
    }

    @Test
    void administrador_noTieneEmpresaNiEquipo() {
        assertThat(lector.porEmail(CORREO)).isEqualTo(new Identidad(7L, null, null));
    }

    @Test
    void siEsEmpleadoYPersonal_laEmpresaEsLaDelPersonal_yConservaElEquipo() {
        when(empleados.findById(7L)).thenReturn(Optional.of(new EmpleadoEquipo(7L, 3L)));
        when(personal.findById(7L)).thenReturn(Optional.of(new PersonalEmpresa(7L, 15L, RolPersonal.COORDINADOR_BIENESTAR)));

        assertThat(lector.porEmail(CORREO)).isEqualTo(new Identidad(7L, 15L, 3L));
    }

    @Test
    void elCorreoSeNormalizaAntesDeBuscar() {
        assertThat(lector.porEmail("  Ana@KALA.test ").uid()).isEqualTo(7L);
    }

    @Test
    void usuarioInexistente_lanzaIllegalState() {
        when(usuarios.findByEmail("nadie@kala.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lector.porEmail("nadie@kala.test")).isInstanceOf(IllegalStateException.class);
    }
}

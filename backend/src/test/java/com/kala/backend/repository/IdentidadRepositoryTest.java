package com.kala.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kala.backend.FilasPadre;
import com.kala.backend.model.AdministradorPlataforma;
import com.kala.backend.model.EmpleadoEquipo;
import com.kala.backend.model.PersonalEmpresa;
import com.kala.backend.model.RolPersonal;
import com.kala.backend.model.Usuario;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

// Necesita la rama de pruebas de Neon (KALA_TEST_DB_URL). Sin esa variable se omite.
@EnabledIfEnvironmentVariable(named = "KALA_TEST_DB_URL", matches = ".+")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class IdentidadRepositoryTest {

    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private EmpleadoEquipoRepository empleados;
    @Autowired
    private PersonalEmpresaRepository personal;
    @Autowired
    private AdministradorPlataformaRepository administradores;
    @Autowired
    private JdbcTemplate jdbc;

    private Usuario nuevoUsuario() {
        return new Usuario("u-" + UUID.randomUUID() + "@kala.test", "hash-de-prueba", "Persona de prueba");
    }

    @Test
    void guardarUsuario_asignaIdYSePuedeBuscarPorEmail() {
        Usuario guardado = usuarios.saveAndFlush(nuevoUsuario());

        assertThat(guardado.getId()).isNotNull();
        assertThat(usuarios.findByEmail(guardado.getEmail())).isPresent();
        assertThat(usuarios.findByEmail("no-existe@kala.test")).isEmpty();
    }

    @Test
    void guardarUsuario_quedaActivoPorDefecto() {
        Usuario guardado = usuarios.saveAndFlush(nuevoUsuario());

        assertThat(usuarios.findById(guardado.getId()).orElseThrow().isActivo()).isTrue();
    }

    @Test
    void guardarDosUsuariosConElMismoEmail_violaLaRestriccionUnica() {
        Usuario primero = usuarios.saveAndFlush(nuevoUsuario());
        Usuario repetido = new Usuario(primero.getEmail(), "otro-hash", "Otra persona");

        assertThatThrownBy(() -> usuarios.saveAndFlush(repetido))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void guardarEmpleadoEquipo_seRecuperaPorUsuario() {
        Long usuarioId = usuarios.saveAndFlush(nuevoUsuario()).getId();

        Long equipoId = FilasPadre.equipo(jdbc, FilasPadre.empresa(jdbc));

        empleados.saveAndFlush(new EmpleadoEquipo(usuarioId, equipoId));

        assertThat(empleados.findById(usuarioId).orElseThrow().getEquipoId()).isEqualTo(equipoId);
    }

    @Test
    void guardarPersonalEmpresa_conRolValido_seRecupera() {
        Long usuarioId = usuarios.saveAndFlush(nuevoUsuario()).getId();

        Long empresaId = FilasPadre.empresa(jdbc);

        personal.saveAndFlush(new PersonalEmpresa(usuarioId, empresaId, RolPersonal.COORDINADOR_BIENESTAR));

        PersonalEmpresa recuperado = personal.findById(usuarioId).orElseThrow();
        assertThat(recuperado.getEmpresaId()).isEqualTo(empresaId);
        assertThat(recuperado.getRol()).isEqualTo(RolPersonal.COORDINADOR_BIENESTAR);
    }

    @Test
    void insertarPersonalEmpresaConRolInvalido_loRechazaElCheckDeLaBase() {
        Long usuarioId = usuarios.saveAndFlush(nuevoUsuario()).getId();
        Long empresaId = FilasPadre.empresa(jdbc);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO personal_empresa (usuario_id, empresa_id, rol) VALUES (?, ?, 'INVALIDO')",
                usuarioId, empresaId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void guardarAdministradorPlataforma_seRecuperaYSeDetectaConExistsById() {
        Long usuarioId = usuarios.saveAndFlush(nuevoUsuario()).getId();

        administradores.saveAndFlush(new AdministradorPlataforma(usuarioId));

        assertThat(administradores.existsById(usuarioId)).isTrue();
        assertThat(administradores.existsById(-1L)).isFalse();
    }
}

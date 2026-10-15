package com.kala.backend.security;

import com.kala.backend.model.EmpleadoEquipo;
import com.kala.backend.model.PersonalEmpresa;
import com.kala.backend.model.Usuario;
import com.kala.backend.repository.EmpleadoEquipoRepository;
import com.kala.backend.repository.PersonalEmpresaRepository;
import com.kala.backend.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Reúne los datos de identidad que viajan en el token: la persona, su empresa y, si es empleado, su equipo.
@Service
public class IdentidadLector {

    // empresaId es null para el administrador de la plataforma; equipoId, para quien no es empleado.
    public record Identidad(Long uid, Long empresaId, Long equipoId) {
    }

    private final UsuarioRepository usuarios;
    private final EmpleadoEquipoRepository empleados;
    private final PersonalEmpresaRepository personal;
    private final EmpresaDeEquipoLector empresas;

    public IdentidadLector(UsuarioRepository usuarios,
                           EmpleadoEquipoRepository empleados,
                           PersonalEmpresaRepository personal,
                           EmpresaDeEquipoLector empresas) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.personal = personal;
        this.empresas = empresas;
    }

    @Transactional(readOnly = true)
    public Identidad porEmail(String email) {
        Usuario usuario = usuarios.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no existe"));
        Long id = usuario.getId();

        Long equipoId = empleados.findById(id).map(EmpleadoEquipo::getEquipoId).orElse(null);
        // Si es personal de empresa, esa es su empresa; si solo es empleado, se deriva de su equipo.
        Long empresaId = personal.findById(id).map(PersonalEmpresa::getEmpresaId).orElse(null);
        if (empresaId == null && equipoId != null) {
            empresaId = empresas.empresaDelEquipo(equipoId).orElse(null);
        }
        return new Identidad(id, empresaId, equipoId);
    }
}

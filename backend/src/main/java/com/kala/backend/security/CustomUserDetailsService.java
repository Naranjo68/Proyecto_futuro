package com.kala.backend.security;

import com.kala.backend.model.Usuario;
import com.kala.backend.repository.AdministradorPlataformaRepository;
import com.kala.backend.repository.EmpleadoEquipoRepository;
import com.kala.backend.repository.PersonalEmpresaRepository;
import com.kala.backend.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Convierte un usuario de la base de datos en un UserDetails de Spring Security.
// Los roles no se guardan: se derivan de los subtipos de identidad. Una persona puede tener varios
// (por ejemplo, ser empleada de un equipo y además RRHH).
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;
    private final EmpleadoEquipoRepository empleados;
    private final PersonalEmpresaRepository personal;
    private final AdministradorPlataformaRepository administradores;

    public CustomUserDetailsService(UsuarioRepository usuarios,
                                    EmpleadoEquipoRepository empleados,
                                    PersonalEmpresaRepository personal,
                                    AdministradorPlataformaRepository administradores) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.personal = personal;
        this.administradores = administradores;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
        // Mensaje genérico: no se revela si el correo existe.
        Usuario usuario = usuarios.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        Long id = usuario.getId();
        List<String> roles = new ArrayList<>();
        if (empleados.existsById(id)) {
            roles.add("ROLE_EMPLEADO");
        }
        personal.findById(id).ifPresent(p -> roles.add("ROLE_" + p.getRol().name()));
        if (administradores.existsById(id)) {
            roles.add("ROLE_ADMIN");
        }

        return User.withUsername(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .authorities(roles.toArray(String[]::new))
                .disabled(!usuario.isActivo())
                .build();
    }
}

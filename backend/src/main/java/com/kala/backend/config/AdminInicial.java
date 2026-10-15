package com.kala.backend.config;

import com.kala.backend.model.AdministradorPlataforma;
import com.kala.backend.model.Usuario;
import com.kala.backend.repository.AdministradorPlataformaRepository;
import com.kala.backend.repository.UsuarioRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Crea el administrador de la plataforma al arrancar, a partir de KALA_ADMIN_EMAIL y KALA_ADMIN_PASSWORD.
// Es idempotente y nunca lleva una contraseña fija en el código. Si faltan las variables, no crea nada.
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);
    private static final int LARGO_MINIMO_CLAVE = 12;

    // Se instancia aquí para no depender del SecurityConfig.
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private final UsuarioRepository usuarios;
    private final AdministradorPlataformaRepository administradores;
    private final String email;
    private final String clave;

    public AdminInicial(UsuarioRepository usuarios,
                        AdministradorPlataformaRepository administradores,
                        @Value("${kala.admin.email:}") String email,
                        @Value("${kala.admin.password:}") String clave) {
        this.usuarios = usuarios;
        this.administradores = administradores;
        this.email = email;
        this.clave = clave;
    }

    @Override
    public void run(String... args) {
        crearSiHaceFalta();
    }

    void crearSiHaceFalta() {
        if (email == null || email.isBlank() || clave == null || clave.isBlank()) {
            log.warn("Administrador inicial no configurado (KALA_ADMIN_EMAIL y KALA_ADMIN_PASSWORD): no se crea ninguno");
            return;
        }
        if (clave.length() < LARGO_MINIMO_CLAVE) {
            log.warn("KALA_ADMIN_PASSWORD debe tener al menos {} caracteres: no se crea el administrador", LARGO_MINIMO_CLAVE);
            return;
        }

        String correo = email.strip().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarios.findByEmail(correo)
                .orElseGet(() -> usuarios.save(new Usuario(correo, encoder.encode(clave), "Administrador")));

        // Si el usuario ya existía pero le faltaba la fila de administrador, solo se agrega esa fila.
        if (!administradores.existsById(usuario.getId())) {
            administradores.save(new AdministradorPlataforma(usuario.getId()));
            log.info("Administrador inicial creado");
        }
    }
}

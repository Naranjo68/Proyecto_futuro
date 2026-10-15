package com.kala.backend.controller;

import com.kala.backend.dto.ReporteAdminResponse;
import com.kala.backend.repository.AdministradorPlataformaRepository;
import com.kala.backend.repository.EmpleadoEquipoRepository;
import com.kala.backend.repository.PersonalEmpresaRepository;
import com.kala.backend.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Reporte solo para ADMIN: la fecha del servidor y el total de usuarios por subtipo.
@RestController
@RequestMapping("/api/admin")
public class AdminReporteController {

    private final UsuarioRepository usuarios;
    private final EmpleadoEquipoRepository empleados;
    private final PersonalEmpresaRepository personal;
    private final AdministradorPlataformaRepository administradores;
    private final ZoneId zona;

    public AdminReporteController(UsuarioRepository usuarios,
                                  EmpleadoEquipoRepository empleados,
                                  PersonalEmpresaRepository personal,
                                  AdministradorPlataformaRepository administradores,
                                  @Value("${kala.ventana.zona}") String zona) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.personal = personal;
        this.administradores = administradores;
        this.zona = ZoneId.of(zona);
    }

    @GetMapping("/reporte")
    @PreAuthorize("hasRole('ADMIN')")
    public ReporteAdminResponse reporte() {
        return new ReporteAdminResponse(
                LocalDate.now(zona),
                usuarios.count(),
                empleados.count(),
                personal.count(),
                administradores.count());
    }
}

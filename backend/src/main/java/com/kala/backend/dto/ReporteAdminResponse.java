package com.kala.backend.dto;

import java.time.LocalDate;

// Resumen para el administrador de la plataforma: solo conteos, nunca datos de personas.
public record ReporteAdminResponse(
        LocalDate fecha,
        long totalUsuarios,
        long empleados,
        long personalEmpresa,
        long administradores
) {
}

package com.kala.backend.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

// La ventana del antiduplicado es la semana que empieza el lunes. Se calcula en la zona horaria de la
// aplicación (America/Lima) para que el cambio de semana ocurra a la medianoche local.
public final class VentanaSemanal {

    private VentanaSemanal() {
    }

    // Lunes de la semana a la que pertenece la fecha.
    public static LocalDate inicioDeVentana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    // Lunes de la semana local en la que cae el instante.
    public static LocalDate inicioDeVentana(Instant instante, ZoneId zona) {
        return inicioDeVentana(instante.atZone(zona).toLocalDate());
    }
}

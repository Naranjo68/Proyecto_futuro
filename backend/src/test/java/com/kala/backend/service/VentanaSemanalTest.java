package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class VentanaSemanalTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDate LUNES = LocalDate.of(2026, 10, 12);

    @Test
    void unLunes_esElInicioDeSuPropiaVentana() {
        assertThat(VentanaSemanal.inicioDeVentana(LUNES)).isEqualTo(LUNES);
    }

    @Test
    void cualquierDiaDeLaSemana_devuelveElMismoLunes() {
        for (int dia = 0; dia < 7; dia++) {
            assertThat(VentanaSemanal.inicioDeVentana(LUNES.plusDays(dia))).isEqualTo(LUNES);
        }
    }

    @Test
    void elLunesSiguiente_abreUnaVentanaNueva() {
        assertThat(VentanaSemanal.inicioDeVentana(LUNES.plusWeeks(1))).isEqualTo(LUNES.plusWeeks(1));
    }

    @Test
    void domingoEnLima_perteneceALaSemanaQueTermina_aunqueEnUtcYaSeaLunes() {
        // 2026-10-19T04:30Z = domingo 2026-10-18 23:30 en Lima (UTC-5)
        Instant domingoNocheEnLima = Instant.parse("2026-10-19T04:30:00Z");

        assertThat(VentanaSemanal.inicioDeVentana(domingoNocheEnLima, LIMA)).isEqualTo(LUNES);
    }

    @Test
    void lunesEnLima_abreLaVentanaNueva() {
        // 2026-10-19T05:30Z = lunes 2026-10-19 00:30 en Lima
        Instant lunesMadrugadaEnLima = Instant.parse("2026-10-19T05:30:00Z");

        assertThat(VentanaSemanal.inicioDeVentana(lunesMadrugadaEnLima, LIMA)).isEqualTo(LUNES.plusWeeks(1));
    }
}

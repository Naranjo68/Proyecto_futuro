package com.kala.backend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CheckInTest {

    @Test
    void registrarRespuestaSiIncrementaTotalYRespuestasSi() {
        CheckIn checkIn = new CheckIn(
                1L,
                10L,
                LocalDate.now()
        );

        checkIn.registrar(true);

        assertThat(checkIn.getTotalRespuestas()).isEqualTo(1);
        assertThat(checkIn.getRespuestasSi()).isEqualTo(1);
    }

    @Test
    void registrarRespuestaNoIncrementaSoloTotal() {
        CheckIn checkIn = new CheckIn(
                1L,
                10L,
                LocalDate.now()
        );

        checkIn.registrar(false);

        assertThat(checkIn.getTotalRespuestas()).isEqualTo(1);
        assertThat(checkIn.getRespuestasSi()).isEqualTo(0);
    }

    @Test
    void registrarVariasRespuestasCalculaCorrectamente() {
        CheckIn checkIn = new CheckIn(
                1L,
                10L,
                LocalDate.now()
        );

        checkIn.registrar(true);
        checkIn.registrar(false);
        checkIn.registrar(true);

        assertThat(checkIn.getTotalRespuestas()).isEqualTo(3);
        assertThat(checkIn.getRespuestasSi()).isEqualTo(2);
    }
}
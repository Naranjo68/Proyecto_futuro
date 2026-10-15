package com.kala.backend.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Test unitario del modelo: JUnit 5 + AssertJ, sin Spring.
class CheckInTest {

    private CheckIn checkIn;

    @BeforeEach
    void setUp() {
        checkIn = new CheckIn(1L, 10L, LocalDate.now());
    }

    @Test
    void registrarRespuestaSi_incrementaTotalYRespuestasSi() {
        checkIn.registrar(true);

        assertThat(checkIn.getTotalRespuestas()).isEqualTo(1);
        assertThat(checkIn.getRespuestasSi()).isEqualTo(1);
    }

    @Test
    void registrarRespuestaNo_incrementaSoloElTotal() {
        checkIn.registrar(false);

        assertThat(checkIn.getTotalRespuestas()).isEqualTo(1);
        assertThat(checkIn.getRespuestasSi()).isEqualTo(0);
    }

    @Test
    void registrarVariasRespuestas_cuentaCadaUna() {
        checkIn.registrar(true);
        checkIn.registrar(false);
        checkIn.registrar(true);

        assertThat(checkIn.getTotalRespuestas()).isEqualTo(3);
        assertThat(checkIn.getRespuestasSi()).isEqualTo(2);
    }
}

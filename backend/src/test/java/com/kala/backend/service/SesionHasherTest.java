package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SesionHasherTest {

    private static final String SECRETO = "secreto-de-prueba";
    private static final LocalDate VENTANA = LocalDate.of(2026, 10, 12);

    @Test
    void elHashCoincideConElHmacSha256CalculadoConOpenSsl() {
        // echo -n "7|3|2026-10-12" | openssl dgst -sha256 -hmac "secreto-de-prueba"
        assertThat(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA))
                .isEqualTo("62029a82e04f3b6b8f31472513b67444fd7db6785d35887879475db3f688acfc");
    }

    @Test
    void elHashTiene64CaracteresHexadecimalesEnMinuscula() {
        assertThat(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA)).matches("[0-9a-f]{64}");
    }

    @Test
    void mismosDatos_danElMismoHash() {
        assertThat(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA))
                .isEqualTo(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA));
    }

    @Test
    void cambiarElUsuario_cambiaElHash() {
        assertThat(SesionHasher.hashDeSesion(SECRETO, 8L, 3L, VENTANA))
                .isNotEqualTo(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA))
                .isEqualTo("c9bb1602458128c417e813b70c6a6307112720a25de235181be924e236a72e72");
    }

    @Test
    void cambiarElCuestionario_cambiaElHash() {
        assertThat(SesionHasher.hashDeSesion(SECRETO, 7L, 4L, VENTANA))
                .isNotEqualTo(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA));
    }

    @Test
    void cambiarLaVentana_cambiaElHash_yNoSePuedeEnlazarEntreSemanas() {
        assertThat(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA.plusWeeks(1)))
                .isNotEqualTo(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA));
    }

    @Test
    void cambiarElSecreto_cambiaElHash() {
        assertThat(SesionHasher.hashDeSesion("otro-secreto", 7L, 3L, VENTANA))
                .isNotEqualTo(SesionHasher.hashDeSesion(SECRETO, 7L, 3L, VENTANA));
    }

    @Test
    void elDelimitadorEvitaColisionesEntreCombinacionesDistintas() {
        // 1|23 y 12|3 no deben producir el mismo mensaje.
        assertThat(SesionHasher.hashDeSesion(SECRETO, 1L, 23L, VENTANA))
                .isNotEqualTo(SesionHasher.hashDeSesion(SECRETO, 12L, 3L, VENTANA));
    }

    @Test
    void sinSecreto_lanzaIllegalStateException() {
        assertThatThrownBy(() -> SesionHasher.hashDeSesion(" ", 7L, 3L, VENTANA))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> SesionHasher.hashDeSesion(null, 7L, 3L, VENTANA))
                .isInstanceOf(IllegalStateException.class);
    }
}

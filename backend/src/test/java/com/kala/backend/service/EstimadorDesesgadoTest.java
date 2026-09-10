package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

// Test unitario de la clase pura: JUnit 5 + AssertJ, sin Spring.
class EstimadorDesesgadoTest {

    private final EstimadorDesesgado estimador = new EstimadorDesesgado();

    @Test
    void estimarProporcion_conDatosValidos_devuelveLaFraccion() {
        assertThat(estimador.estimarProporcion(3, 4)).isEqualTo(0.75);
    }

    @Test
    void estimarProporcion_conTotalCero_lanzaIllegalStateException() {
        assertThatThrownBy(() -> estimador.estimarProporcion(1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}

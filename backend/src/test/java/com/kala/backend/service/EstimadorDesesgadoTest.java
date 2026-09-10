package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstimadorDesesgadoTest {

    private EstimadorDesesgado estimador;

    @BeforeEach
    void setUp() {
        estimador = new EstimadorDesesgado();
    }

    @Test
    void cuandoRespuestasValidas_debeRetornarProporcionCorrecta() {
        double resultado = estimador.estimarProporcion(3, 4);
        assertThat(resultado).isEqualTo(0.75);
    }

    @Test
    void cuandoTotalEsCero_debeLanzarIllegalStateException() {
        assertThatThrownBy(() -> estimador.estimarProporcion(1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No hay respuestas para estimar");
    }
}
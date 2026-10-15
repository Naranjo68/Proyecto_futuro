package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraPresupuestoTest {

    private final CalculadoraPresupuesto calculadora = new CalculadoraPresupuesto();

    @Test
    void randomizedResponseConP075_daLnDeTres() {
        assertThat(calculadora.epsilonItem("RANDOMIZED_RESPONSE", new BigDecimal("0.750"), null))
                .isCloseTo(1.0986, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    void laplace_usaElEpsilonConfigurado() {
        assertThat(calculadora.epsilonItem("LAPLACE", null, new BigDecimal("0.500"))).isEqualTo(0.5);
    }

    @Test
    void configuracionIncompletaOMecanismoDesconocido_sonRechazados() {
        assertThatThrownBy(() -> calculadora.epsilonItem("RANDOMIZED_RESPONSE", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculadora.epsilonItem("RANDOMIZED_RESPONSE", new BigDecimal("0.500"), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculadora.epsilonItem("LAPLACE", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculadora.epsilonItem("OTRO", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pulsoSemanalConP075_gastaAproximadamente549AlMes() {
        double eps = calculadora.epsilonItem("RANDOMIZED_RESPONSE", new BigDecimal("0.750"), null);

        assertThat(calculadora.epsilonMensual(7, null, List.of(eps)))
                .isCloseTo(5.49, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void pulsoDiarioConP075_gastaAproximadamente3296AlMes() {
        double eps = calculadora.epsilonItem("RANDOMIZED_RESPONSE", new BigDecimal("0.750"), null);

        assertThat(calculadora.epsilonMensual(1, null, List.of(eps)))
                .isCloseTo(32.96, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void kNulo_usaElTotalDeItems_yKExplicitoLoLimita() {
        List<Double> tresItems = List.of(1.0, 1.0, 1.0);

        // 30 / 7 -> 5 envíos
        assertThat(calculadora.epsilonMensual(7, null, tresItems)).isEqualTo(15.0);
        assertThat(calculadora.epsilonMensual(7, 2, tresItems)).isEqualTo(10.0);
    }

    @Test
    void sinItemsNoGastaNada_yUnaCadenciaNoPositivaSeRechaza() {
        assertThat(calculadora.epsilonMensual(7, null, List.of())).isZero();
        assertThatThrownBy(() -> calculadora.epsilonMensual(0, null, List.of(1.0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

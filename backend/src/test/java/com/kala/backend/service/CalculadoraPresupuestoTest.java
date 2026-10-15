package com.kala.backend.service;

import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.ItemCuestionario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class CalculadoraPresupuestoTest {

    private CalculadoraPresupuesto calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraPresupuesto();
    }

    @Test
    @DisplayName("Epsilon de item con Randomized Response p = 0.75 es aprox 1.0986")
    void epsilonItemRandomizedResponse() {
        ConfiguracionLdp config = new ConfiguracionLdp();
        config.setMecanismo("RANDOMIZED_RESPONSE");
        config.setProbabilidadVerdad(new BigDecimal("0.750"));

        double epsilon = calculadora.calcularEpsilonItem(config);
        assertThat(epsilon).isCloseTo(1.0986, offset(0.001));
    }

    @Test
    @DisplayName("Pulso semanal con p = 0.75 da aproximadamente 5.49")
    void pulsoSemanalMensual() {
        ConfiguracionLdp config = new ConfiguracionLdp();
        config.setMecanismo("RANDOMIZED_RESPONSE");
        config.setProbabilidadVerdad(new BigDecimal("0.750"));

        ItemCuestionario item = new ItemCuestionario();
        item.setConfiguracionLdp(config);

        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setCadenciaDias((short) 7);
        cuestionario.getItems().add(item);

        double total = calculadora.calcularEpsilonMensualCuestionario(cuestionario);
        assertThat(total).isCloseTo(5.49, offset(0.01));
    }

    @Test
    @DisplayName("Pulso diario con p = 0.75 da aproximadamente 32.96")
    void pulsoDiarioMensual() {
        ConfiguracionLdp config = new ConfiguracionLdp();
        config.setMecanismo("RANDOMIZED_RESPONSE");
        config.setProbabilidadVerdad(new BigDecimal("0.750"));

        ItemCuestionario item = new ItemCuestionario();
        item.setConfiguracionLdp(config);

        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setCadenciaDias((short) 1);
        cuestionario.getItems().add(item);

        double total = calculadora.calcularEpsilonMensualCuestionario(cuestionario);
        assertThat(total).isCloseTo(32.96, offset(0.01));
    }

    @Test
    @DisplayName("Cuestionario con k nulo usa el total de ítems")
    void kNuloUsaTotalItems() {
        ConfiguracionLdp config = new ConfiguracionLdp();
        config.setMecanismo("LAPLACE");
        config.setEpsilon(new BigDecimal("2.000"));

        ItemCuestionario item1 = new ItemCuestionario();
        item1.setConfiguracionLdp(config);
        ItemCuestionario item2 = new ItemCuestionario();
        item2.setConfiguracionLdp(config);

        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setCadenciaDias((short) 30); // 1 envío al mes
        cuestionario.setItemsPorEnvio(null); // k nulo -> 2 ítems
        cuestionario.getItems().add(item1);
        cuestionario.getItems().add(item2);

        double total = calculadora.calcularEpsilonMensualCuestionario(cuestionario);
        // envios=1 * k=2 * eps=2.0 = 4.0
        assertThat(total).isCloseTo(4.0, offset(0.001));
    }
}

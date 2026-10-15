package com.kala.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraPresupuestoTest {

    @Test
    @DisplayName("Calcula epsilon correctamente para Randomized Response con p=0.75")
    void calculaEpsilonRandomizedResponse() {
        double epsilon = CalculadoraPresupuesto.calcularEpsilonItem("RANDOMIZED_RESPONSE", 0.75, null);
        assertEquals(1.0986, epsilon, 0.001);
    }

    @Test
    @DisplayName("Calcula el epsilon mensual para pulso semanal")
    void calculaEpsilonMensualPulsoSemanal() {
        // 30 / 7 = 5 envíos al mes. 5 * 1 ítem * 1.0986 = 5.493
        double epsilonMensual = CalculadoraPresupuesto.calcularEpsilonMensual(7, 1, 1.0986);
        assertEquals(5.493, epsilonMensual, 0.001);
    }
}

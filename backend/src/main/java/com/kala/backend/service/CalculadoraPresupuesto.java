package com.kala.backend.service;

import java.math.BigDecimal;
import java.util.List;

// Clase pura: sin Spring y sin estado. Calcula cuánta privacidad (ε) gasta una persona.
public class CalculadoraPresupuesto {

    private static final int DIAS_DEL_MES = 30;

    // ε de un ítem. Randomized Response: ln(p / (1 − p)). Laplace: el epsilon configurado.
    public double epsilonItem(String mecanismo, BigDecimal probabilidadVerdad, BigDecimal epsilon) {
        if ("RANDOMIZED_RESPONSE".equals(mecanismo)) {
            if (probabilidadVerdad == null) {
                throw new IllegalArgumentException("Randomized Response requiere probabilidadVerdad");
            }
            double p = probabilidadVerdad.doubleValue();
            if (p <= 0.5 || p >= 1.0) {
                throw new IllegalArgumentException("p debe ser mayor que 0.5 y menor que 1.0");
            }
            return Math.log(p / (1.0 - p));
        }
        if ("LAPLACE".equals(mecanismo)) {
            if (epsilon == null || epsilon.signum() <= 0) {
                throw new IllegalArgumentException("Laplace requiere un epsilon positivo");
            }
            return epsilon.doubleValue();
        }
        throw new IllegalArgumentException("Mecanismo desconocido: " + mecanismo);
    }

    // ε mensual de una persona en un cuestionario: ⌈30 / cadenciaDias⌉ envíos × k ítems × ε del ítem.
    // k es itemsPorEnvio, o el total de ítems si es nulo. Si los ítems tienen ε distintos se usa su promedio.
    public double epsilonMensual(int cadenciaDias, Integer itemsPorEnvio, List<Double> epsilonsDeLosItems) {
        if (cadenciaDias <= 0) {
            throw new IllegalArgumentException("La cadencia debe ser positiva");
        }
        if (epsilonsDeLosItems == null || epsilonsDeLosItems.isEmpty()) {
            return 0.0;
        }
        int k = itemsPorEnvio != null ? itemsPorEnvio : epsilonsDeLosItems.size();
        double promedio = epsilonsDeLosItems.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        long envios = (DIAS_DEL_MES + cadenciaDias - 1) / cadenciaDias;
        return envios * k * promedio;
    }
}

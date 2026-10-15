package com.kala.backend.service;

public class CalculadoraPresupuesto {
    public static double calcularEpsilonItem(String mecanismo, Double probabilidadVerdad, Double epsilon) {
        if ("RANDOMIZED_RESPONSE".equals(mecanismo) && probabilidadVerdad != null) {
            return Math.log(probabilidadVerdad / (1.0 - probabilidadVerdad));
        }
        return epsilon != null ? epsilon : 0.0;
    }

    public static double calcularEpsilonMensual(int cadenciaDias, int itemsPorEnvio, double epsilonItem) {
        int enviosMes = (int) Math.ceil(30.0 / cadenciaDias);
        return enviosMes * itemsPorEnvio * epsilonItem;
    }
}

package com.kala.backend.service;

import com.kala.backend.model.ConfiguracionLdp;
import com.kala.backend.model.Cuestionario;
import com.kala.backend.model.ItemCuestionario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CalculadoraPresupuesto {

    public double calcularEpsilonItem(ConfiguracionLdp config) {
        if ("RANDOMIZED_RESPONSE".equalsIgnoreCase(config.getMecanismo())) {
            BigDecimal p = config.getProbabilidadVerdad();
            if (p == null) {
                throw new IllegalArgumentException("Probabilidad de verdad requerida para RANDOMIZED_RESPONSE");
            }
            double prob = p.doubleValue();
            return Math.log(prob / (1.0 - prob));
        } else if ("LAPLACE".equalsIgnoreCase(config.getMecanismo())) {
            BigDecimal eps = config.getEpsilon();
            if (eps == null) {
                throw new IllegalArgumentException("Epsilon requerido para LAPLACE");
            }
            return eps.doubleValue();
        }
        throw new IllegalArgumentException("Mecanismo no soportado: " + config.getMecanismo());
    }

    public double calcularEpsilonMensualCuestionario(Cuestionario cuestionario) {
        if (cuestionario.getCadenciaDias() <= 0) {
            throw new IllegalArgumentException("La cadencia de días debe ser mayor a 0");
        }

        long enviosPorMes = (long) Math.ceil(30.0 / cuestionario.getCadenciaDias());
        
        int k = (cuestionario.getItemsPorEnvio() != null && cuestionario.getItemsPorEnvio() > 0)
                ? cuestionario.getItemsPorEnvio()
                : cuestionario.getItems().size();

        double epsilonSumItems = 0.0;
        for (ItemCuestionario item : cuestionario.getItems()) {
            epsilonSumItems += calcularEpsilonItem(item.getConfiguracionLdp());
        }

        // Si k es menor que el total de ítems, promediamos el epsilon por ítem
        double epsilonPromedioItem = cuestionario.getItems().isEmpty() ? 0.0 : epsilonSumItems / cuestionario.getItems().size();
        
        return enviosPorMes * k * epsilonPromedioItem;
    }
}

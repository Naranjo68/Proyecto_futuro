package com.kala.backend.service;

// Clase pura: sin Spring y sin estado. Devuelve respuestasSi / total.
public class EstimadorDesesgado {

    private final double p;

    public EstimadorDesesgado(double p){
        if (!Double.isFinite(p) || p <= 0.5 || p >= 1.0){
            throw new IllegalArgumentException(
                "p debe ser mayor que 0.5 y menor que 1.0"
            );
        }

        this.p = p;
    }

    public double estimarProporcion(int respuestasSi, int total){
        if (total == 0){
            throw new IllegalStateException(
                "No se puede estimar con cero respuestas"
            );
        }

        double lambda = (double) respuestasSi / total;

        double estimacion = ( lambda - (1.0 -p))/(2.0 *p - 1.0);

        // la proporcion estimada debe permanecer entre 0 y 1.
        return Math.max(0.0, Math.min(1.0, estimacion));
    }

    public double margenError95(int respuestasSi, int total){
        if (total <= 0){
            throw new IllegalStateException(
                "El total de respuestas debe ser positiva"
            );
        }

        double lambda = (double) respuestasSi / total;

        return 1.96 * Math.sqrt(lambda * (1.0 - lambda) / total) / (2.0 * p - 1.0);
    }
}

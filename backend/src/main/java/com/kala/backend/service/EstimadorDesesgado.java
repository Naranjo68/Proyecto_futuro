package com.kala.backend.service;

// Clase pura: sin Spring y sin estado. Devuelve respuestasSi / total.
public class EstimadorDesesgado {

    public double estimarProporcion(int respuestasSi, int total) {
        if (total == 0) {
            throw new IllegalStateException("total no puede ser cero");
        }
        return (double) respuestasSi / total;
    }
}

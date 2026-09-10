package com.kala.backend.service;

public class EstimadorDesesgado {

    public double estimarProporcion(int respuestasSi, int total) {
        if (total == 0) {
            throw new IllegalStateException("No hay respuestas para estimar");
        }
        return (double) respuestasSi / total;
    }
}

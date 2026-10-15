package com.kala.backend.service;

import java.util.random.RandomGenerator;

// Aleatorizador de referencia. El servidor nunca aleatoriza: esto simula lo que hace el dispositivo.
public final class RandomizedResponseReferencia {

    private RandomizedResponseReferencia() {
    }

    // Devuelve la respuesta real con probabilidad p y la contraria con probabilidad 1 − p.
    public static boolean perturbar(boolean respuestaReal, double p, RandomGenerator azar) {
        return azar.nextDouble() < p ? respuestaReal : !respuestaReal;
    }
}

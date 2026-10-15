
package com.kala.backend.service;

import java.util.random.RandomGenerator;

public final class RandomizedResponseReferencia {

    private RandomizedResponseReferencia() {
    }

    public static boolean perturbar(
            boolean respuestaReal,
            double p,
            RandomGenerator random) {

        if (!Double.isFinite(p) || p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException(
                    "p debe estar entre 0 y 1.");
        }

        if (random == null) {
            throw new IllegalArgumentException(
                    "El generador aleatorio es obligatorio.");
        }

        boolean conservar = random.nextDouble() < p;

        return conservar ? respuestaReal : !respuestaReal;
    }
}

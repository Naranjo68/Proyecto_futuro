
package com.kala.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.Test;

class RandomizedResponseReferenciaTest {

    @Test
    void estimaProporcionRealConErrorMaximoDe001() {
        double p = 0.75;
        double proporcionReal = 0.30;
        int total = 100_000;

        RandomGenerator random =
                RandomGeneratorFactory.of("L64X128MixRandom")
                        .create(123456789L);

        int respuestasSi = 0;

        for (int i = 0; i < total; i++) {
            boolean real = random.nextDouble() < proporcionReal;

            boolean perturbada =
                    RandomizedResponseReferencia.perturbar(
                            real, p, random);

            if (perturbada) {
                respuestasSi++;
            }
        }

        double lambda = (double) respuestasSi / total;
        double estimacion = (lambda - (1.0 - p)) / (2.0 * p - 1.0);

        assertEquals(proporcionReal, estimacion, 0.01);
    }
}


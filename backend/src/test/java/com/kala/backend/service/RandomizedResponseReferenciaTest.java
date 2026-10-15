package com.kala.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class RandomizedResponseReferenciaTest {

    private static final int RESPUESTAS = 100_000;
    // La semilla se fija para que la prueba sea repetible; no se eligió por el resultado.
    private static final long SEMILLA = 42L;

    private static RandomGenerator azar() {
        return RandomGeneratorFactory.of("Xoshiro256PlusPlus").create(SEMILLA);
    }

    @Test
    void conPUno_devuelveSiempreLaRespuestaReal() {
        RandomGenerator azar = azar();

        for (int i = 0; i < 1_000; i++) {
            assertThat(RandomizedResponseReferencia.perturbar(true, 1.0, azar)).isTrue();
            assertThat(RandomizedResponseReferencia.perturbar(false, 1.0, azar)).isFalse();
        }
    }

    @Test
    void conPCero_devuelveSiempreLaContraria() {
        RandomGenerator azar = azar();

        for (int i = 0; i < 1_000; i++) {
            assertThat(RandomizedResponseReferencia.perturbar(true, 0.0, azar)).isFalse();
        }
    }

    @Test
    void montecarlo_conPiReal030_elEstimadorRecuperaLaProporcionDentroDeUnPuntoPorCiento() {
        double p = 0.75;
        double piReal = 0.30;
        RandomGenerator azar = azar();

        int si = 0;
        for (int i = 0; i < RESPUESTAS; i++) {
            boolean real = azar.nextDouble() < piReal;
            if (RandomizedResponseReferencia.perturbar(real, p, azar)) {
                si++;
            }
        }

        double estimada = new EstimadorDesesgado(p).estimarProporcion(si, RESPUESTAS);

        assertThat(estimada).isBetween(piReal - 0.01, piReal + 0.01);
    }
}

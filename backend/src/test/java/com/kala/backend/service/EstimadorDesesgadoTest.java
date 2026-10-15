package com.kala.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstimadorDesesgadoTest {

    private static final double DELTA = 0.001;

    @Test
    void estimaProporcionDe65Sobre100() {
        EstimadorDesesgado estimador = new EstimadorDesesgado(0.75);

        assertEquals(
            0.80,
            estimador.estimarProporcion(65, 100),
            DELTA
        );
    }

    @Test
    void estimaProporcionDe50Sobre100() {
        EstimadorDesesgado estimador = new EstimadorDesesgado(0.75);

        assertEquals(
            0.50,
            estimador.estimarProporcion(50, 100),
            DELTA
        );
    }

    @Test
    void recortaEstimacionInferiorACero() {
        EstimadorDesesgado estimador = new EstimadorDesesgado(0.75);

        assertEquals(
            0.0,
            estimador.estimarProporcion(10, 100),
            DELTA
        );
    }

    @Test
    void recortaEstimacionSuperiorAUno() {
        EstimadorDesesgado estimador = new EstimadorDesesgado(0.75);

        assertEquals(
            1.0,
            estimador.estimarProporcion(100, 100),
            DELTA
        );
    }

    @Test
    void totalCeroLanzaIllegalStateException() {
        EstimadorDesesgado estimador = new EstimadorDesesgado(0.75);

        assertThrows(
            IllegalStateException.class,
            () -> estimador.estimarProporcion(0, 0)
        );
    }

    @Test
    void pIgualAMedioLanzaIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new EstimadorDesesgado(0.5)
        );
    }

    @Test
    void calculaMargenDeErrorDel95PorCiento() {
        EstimadorDesesgado estimador = new EstimadorDesesgado(0.75);

        double margen100 = estimador.margenError95(50, 100);
        double margen30 = estimador.margenError95(15, 30);

        assertEquals(0.196, margen100, DELTA);
        assertEquals(0.358, margen30, DELTA);
    }
}


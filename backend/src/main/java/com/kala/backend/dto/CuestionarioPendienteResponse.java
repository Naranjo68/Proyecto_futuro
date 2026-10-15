package com.kala.backend.dto;

import java.math.BigDecimal;
import java.util.List;

// Lo que el dispositivo necesita para mostrar un cuestionario y aleatorizar cada respuesta antes de enviarla.
public record CuestionarioPendienteResponse(
        Long id,
        String codigo,
        String nombre,
        int cadenciaDias,
        Integer itemsPorEnvio,
        List<ItemPendiente> items
) {

    public record ItemPendiente(
            Long itemId,
            int orden,
            String tema,
            String texto,
            Escala escala,
            Long configuracionLdpId,
            String mecanismo,
            BigDecimal probabilidadVerdad
    ) {
    }

    public record Escala(String codigo, int valorMin, int valorMax, List<Opcion> opciones) {
    }

    public record Opcion(int valor, String etiqueta) {
    }
}

package com.kala.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// Anonimizado de la sesión de check-in: HMAC-SHA256(secreto, usuario | cuestionario | ventana).
// Sirve solo para impedir un segundo envío en la misma ventana. El hash incluye la ventana, así que
// cada semana produce un valor distinto y no enlazable con el de la semana anterior. Nunca se guarda
// junto al usuario ni al equipo.
public final class SesionHasher {

    private static final String ALGORITMO = "HmacSHA256";

    private SesionHasher() {
    }

    public static String hashDeSesion(String secreto, Long usuarioId, Long cuestionarioId, LocalDate ventanaInicio) {
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException("El secreto de sesión (KALA_SESSION_SECRET) no está configurado");
        }
        Objects.requireNonNull(usuarioId, "usuarioId es obligatorio");
        Objects.requireNonNull(cuestionarioId, "cuestionarioId es obligatorio");
        Objects.requireNonNull(ventanaInicio, "ventanaInicio es obligatoria");

        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), ALGORITMO));
            // El separador evita que "1|23" y "12|3" produzcan el mismo mensaje.
            String mensaje = usuarioId + "|" + cuestionarioId + "|" + ventanaInicio;
            return HexFormat.of().formatHex(mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de sesión", e);
        }
    }
}

package com.kala.backend.dto;

// Respuesta del login: nunca incluye la contraseña ni su hash.
public record TokenResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds
) {
}

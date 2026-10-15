package com.kala.backend.exception;

import com.kala.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Único lugar de la app con @ExceptionHandler. Los controllers no llevan try/catch:
// lanzan (o dejan pasar) la excepción y aquí se traduce a un ErrorResponse + código.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Recurso pedido que no existe.
    @ExceptionHandler({
            EmpresaNoEncontradaException.class,
            EquipoNoEncontradoException.class,
            AgregadoNoEncontradoException.class
    })
    public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Falla de @Valid sobre el body: se juntan los mensajes de cada campo inválido.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    // Reglas de negocio validadas en los services.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> argumentoInvalido(IllegalArgumentException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, HttpServletRequest request) {
        ErrorResponse cuerpo = new ErrorResponse(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(estado).body(cuerpo);
    }
}

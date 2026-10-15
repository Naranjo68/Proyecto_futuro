package com.kala.backend.exception;

import com.kala.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Un solo lugar para traducir las excepciones de todos los modulos a una respuesta ErrorResponse con el codigo HTTP
// que corresponde. Cada excepcion de negocio conserva su @ResponseStatus como documentacion del codigo que produce.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: el recurso no existe (o no pertenece a la empresa del token, para no revelar su existencia).
    @ExceptionHandler({
            EmpresaNoEncontradaException.class,
            EquipoNoEncontradoException.class,
            CuestionarioNoEncontradoException.class,
            ItemCuestionarioNoEncontradoException.class
    })
    public ResponseEntity<ErrorResponse> noEncontrado(
            RuntimeException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 409: la operacion choca con el estado actual (nombre repetido, empresa con equipos, check-in ya hecho).
    @ExceptionHandler({
            NombreDuplicadoException.class,
            EmpresaConEquiposException.class,
            CheckInDuplicadoException.class
    })
    public ResponseEntity<ErrorResponse> conflicto(
            RuntimeException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 409: la base rechazo la operacion por una restriccion (por ejemplo un RUC repetido). El mensaje es fijo para no
    // devolver el SQL ni los valores del conflicto.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridadDeDatos(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de los datos", request);
    }

    // 422: la peticion es valida pero una regla de privacidad la rechaza.
    @ExceptionHandler({
            PresupuestoExcedidoException.class,
            ConfiguracionLdpNoVigenteException.class
    })
    public ResponseEntity<ErrorResponse> reglaDePrivacidad(
            RuntimeException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request);
    }

    // 401: el mensaje no distingue entre usuario inexistente y contrasena incorrecta.
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credencialesInvalidas(
            CredencialesInvalidasException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            SemanaInvalidaException.class
    })
    public ResponseEntity<ErrorResponse> argumentoInvalido(
            RuntimeException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 400 con un mensaje fijo: nunca se devuelve el cuerpo recibido (en POST /api/checkin es la respuesta ruidosa).
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroFaltante(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.BAD_REQUEST, "Falta el parámetro: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroMalFormado(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return construir(HttpStatus.BAD_REQUEST, "El parámetro " + ex.getName() + " no tiene el formato esperado", request);
    }

    private ResponseEntity<ErrorResponse> construir(
            HttpStatus estado,
            String mensaje,
            HttpServletRequest request) {

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

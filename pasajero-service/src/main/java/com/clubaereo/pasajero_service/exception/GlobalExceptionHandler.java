package com.clubaereo.pasajero_service.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones del servicio en respuestas HTTP con un JSON uniforme:
 * { "error": "CODIGO", "detalle": "mensaje legible" }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus status, String error, String detalle) {
        return ResponseEntity.status(status).body(Map.of("error", error, "detalle", detalle));
    }

    // 404: el recurso no existe
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(NotFoundException e) {
        return respuesta(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", e.getMessage());
    }

    // 409: choca con el estado actual (ya inscrito, ya firmado, falta peso)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> conflicto(ConflictException e) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO", e.getMessage());
    }

    // 409: red de seguridad si la restricción única de la base detecta un duplicado
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO",
                "El pasajero ya está inscrito en esta jornada");
    }

    // 400: petición inválida (por ejemplo, RUT con dígito verificador incorrecto)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> peticionInvalida(BadRequestException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "PETICION_INVALIDA", e.getMessage());
    }

    // 400: falla una validación de @Valid (peso fuera de rango, campos vacíos, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", detalle);
    }
}

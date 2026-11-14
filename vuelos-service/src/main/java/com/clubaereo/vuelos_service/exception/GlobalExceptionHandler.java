package com.clubaereo.vuelos_service.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.clubaereo.vuelos_service.dto.ErrorResponse;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler (RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // Respaldo: si dos requests simultáneos pasan la validación, la UniqueConstraint de la BD los frena
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "La operación viola una restricción de datos", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", campos);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String msg,
                                                Map<String, String> errores) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), msg, errores));
    }
}
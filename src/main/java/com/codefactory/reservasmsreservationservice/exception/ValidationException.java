package com.codefactory.reservasmsreservationservice.exception;

/**
 * Excepción lanzada cuando hay un error de validación en los datos de entrada.
 */
public class ValidationException extends BusinessException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(message, ERROR_CODE);
    }

    public ValidationException(String message, String details) {
        super(message, ERROR_CODE, details);
    }
}
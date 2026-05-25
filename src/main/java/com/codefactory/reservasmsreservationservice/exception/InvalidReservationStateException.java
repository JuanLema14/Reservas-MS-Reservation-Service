package com.codefactory.reservasmsreservationservice.exception;

/**
 * Excepción lanzada cuando se intenta una operación no válida en una reserva
 * (ej: cancelar una reserva ya completada).
 */
public class InvalidReservationStateException extends BusinessException {

    private static final String DEFAULT_MESSAGE = "Estado de reserva inválido para esta operación";
    private static final String ERROR_CODE = "INVALID_RESERVATION_STATE";

    public InvalidReservationStateException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidReservationStateException(String message) {
        super(message, ERROR_CODE);
    }

    public InvalidReservationStateException(String message, String details) {
        super(message, ERROR_CODE, details);
    }
}
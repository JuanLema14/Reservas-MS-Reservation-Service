package com.codefactory.reservasmsreservationservice.exception;

/**
 * Excepción lanzada cuando hay un conflicto de horarios (horario ya reservado).
 */
public class ReservationConflictException extends BusinessException {

    private static final String DEFAULT_MESSAGE = "Este horario ya no está disponible";
    private static final String ERROR_CODE = "RESERVATION_CONFLICT";

    public ReservationConflictException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public ReservationConflictException(String message) {
        super(message, ERROR_CODE);
    }

    public ReservationConflictException(String message, String details) {
        super(message, ERROR_CODE, details);
    }
}
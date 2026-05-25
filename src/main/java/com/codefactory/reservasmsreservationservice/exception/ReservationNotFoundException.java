package com.codefactory.reservasmsreservationservice.exception;

/**
 * Excepción lanzada cuando una reserva no es encontrada.
 */
public class ReservationNotFoundException extends BusinessException {

    private static final String DEFAULT_MESSAGE = "Reserva no encontrada";
    private static final String ERROR_CODE = "RESERVATION_NOT_FOUND";

    public ReservationNotFoundException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public ReservationNotFoundException(String message) {
        super(message, ERROR_CODE);
    }

    public ReservationNotFoundException(String message, String details) {
        super(message, ERROR_CODE, details);
    }
}
package com.codefactory.reservasmsreservationservice.exception;

/**
 * Excepción lanzada cuando el usuario no tiene permisos para realizar
 * una operación sobre una reserva (ej: un cliente intentando modificar
 * la reserva de otro cliente).
 */
public class ReservationAccessDeniedException extends BusinessException {

    private static final String DEFAULT_MESSAGE = "No tienes permisos para realizar esta operación sobre la reserva";
    private static final String ERROR_CODE = "RESERVATION_ACCESS_DENIED";

    public ReservationAccessDeniedException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public ReservationAccessDeniedException(String message) {
        super(message, ERROR_CODE);
    }

    public ReservationAccessDeniedException(String message, String details) {
        super(message, ERROR_CODE, details);
    }
}
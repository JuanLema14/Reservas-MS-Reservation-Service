package com.codefactory.reservasmsreservationservice.exception;

import com.codefactory.reservasmsreservationservice.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Handler global para todas las excepciones del microservicio.
 * Proporciona respuestas de error consistentes y códigos de error específicos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== Excepciones de Negocio ====================

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleReservationNotFound(
            ReservationNotFoundException ex, HttpServletRequest request) {
        logger.warn("Reserva no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode(), request);
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleReservationConflict(
            ReservationConflictException ex, HttpServletRequest request) {
        logger.warn("Conflicto de reserva: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), ex.getErrorCode(), request);
    }

    @ExceptionHandler(InvalidReservationStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidReservationState(
            InvalidReservationStateException ex, HttpServletRequest request) {
        logger.warn("Estado de reserva inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrorCode(), request);
    }

    @ExceptionHandler(ReservationAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleReservationAccessDenied(
            ReservationAccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Acceso denegado a reserva: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getErrorCode(), request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        logger.warn("Error de validación: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrorCode(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        logger.warn("Excepción de negocio: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrorCode(), request);
    }

    // ==================== Excecciones de Recursos ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND", request);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalServiceException(
            ExternalServiceException ex, HttpServletRequest request) {
        logger.error("Error en servicio externo: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, 
                "Servicio temporalmente no disponible. Intenta más tarde.", 
                "EXTERNAL_SERVICE_ERROR", request);
    }

    // ==================== Excepciones de Seguridad ====================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        logger.warn("Error de autenticación: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, 
                "Debes iniciar sesión para hacer una reserva", 
                "AUTHENTICATION_REQUIRED", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Acceso denegado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, 
                "No tienes permisos para realizar esta acción", 
                "ACCESS_DENIED", request);
    }

    // ==================== Excepciones de Validación ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        logger.warn("Error de validación en request: {}", errors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errors, "VALIDATION_ERROR", request);
    }

    // ==================== Excepciones Genéricas ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Argumento ilegal: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Error inesperado: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Ocurrió un error interno. Por favor, intenta más tarde.", 
                "INTERNAL_ERROR", request);
    }

    // ==================== Métodos Auxiliares ====================

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(
            HttpStatus status, String message, String errorCode, HttpServletRequest request) {
        
        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .codigoError(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(status).body(error);
    }
}
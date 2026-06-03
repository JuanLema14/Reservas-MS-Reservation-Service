package com.codefactory.reservasmsreservationservice.exception;

import com.codefactory.reservasmsreservationservice.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 */
@DisplayName("GlobalExceptionHandler - Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/reservations");
    }

    @Test
    @DisplayName("Debe manejar ReservationNotFoundException con 404")
    void handleReservationNotFound_Returns404() {
        ReservationNotFoundException ex = new ReservationNotFoundException("Reserva no encontrada con ID: 1");
        ResponseEntity<ErrorResponseDTO> response = handler.handleReservationNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getCodigoError()).isEqualTo("RESERVATION_NOT_FOUND");
    }

    @Test
    @DisplayName("Debe manejar ReservationConflictException con 409")
    void handleReservationConflict_Returns409() {
        ReservationConflictException ex = new ReservationConflictException("Horario ocupado");
        ResponseEntity<ErrorResponseDTO> response = handler.handleReservationConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("Debe manejar InvalidReservationStateException con 400")
    void handleInvalidReservationState_Returns400() {
        InvalidReservationStateException ex = new InvalidReservationStateException("Estado inválido");
        ResponseEntity<ErrorResponseDTO> response = handler.handleInvalidReservationState(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Debe manejar ReservationAccessDeniedException con 403")
    void handleReservationAccessDenied_Returns403() {
        ReservationAccessDeniedException ex = new ReservationAccessDeniedException("Sin permiso");
        ResponseEntity<ErrorResponseDTO> response = handler.handleReservationAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Debe manejar ValidationException con 400")
    void handleValidation_Returns400() {
        ValidationException ex = new ValidationException("Campo requerido");
        ResponseEntity<ErrorResponseDTO> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCodigoError()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("Debe manejar BusinessException con 400")
    void handleBusinessException_Returns400() {
        BusinessException ex = new BusinessException("Error de negocio", "CUSTOM_CODE");
        ResponseEntity<ErrorResponseDTO> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCodigoError()).isEqualTo("CUSTOM_CODE");
    }

    @Test
    @DisplayName("Debe manejar ResourceNotFoundException con 404")
    void handleResourceNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");
        ResponseEntity<ErrorResponseDTO> response = handler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCodigoError()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    @DisplayName("Debe manejar ExternalServiceException con 503")
    void handleExternalServiceException_Returns503() {
        ExternalServiceException ex = new ExternalServiceException("AUTH-SERVICE", "Servicio externo caído", 503);
        ResponseEntity<ErrorResponseDTO> response = handler.handleExternalServiceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getCodigoError()).isEqualTo("EXTERNAL_SERVICE_ERROR");
    }

    @Test
    @DisplayName("Debe manejar AuthenticationException con 401")
    void handleAuthenticationException_Returns401() {
        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("No autenticado");

        ResponseEntity<ErrorResponseDTO> response = handler.handleAuthenticationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCodigoError()).isEqualTo("AUTHENTICATION_REQUIRED");
    }

    @Test
    @DisplayName("Debe manejar AccessDeniedException con 403")
    void handleAccessDeniedException_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Acceso denegado");
        ResponseEntity<ErrorResponseDTO> response = handler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCodigoError()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException con 400")
    void handleValidationException_Returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "campo", "El campo es obligatorio");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCodigoError()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).contains("El campo es obligatorio");
    }

    @Test
    @DisplayName("Debe manejar IllegalArgumentException con 400")
    void handleIllegalArgument_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento no válido");
        ResponseEntity<ErrorResponseDTO> response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCodigoError()).isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    @DisplayName("Debe manejar Exception genérica con 500")
    void handleGenericException_Returns500() {
        Exception ex = new RuntimeException("Error inesperado");
        ResponseEntity<ErrorResponseDTO> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCodigoError()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("La respuesta de error debe incluir path y timestamp")
    void errorResponse_IncludesPathAndTimestamp() {
        ReservationNotFoundException ex = new ReservationNotFoundException("Test");
        ResponseEntity<ErrorResponseDTO> response = handler.handleReservationNotFound(ex, request);

        ErrorResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getPath()).isEqualTo("/api/reservations");
        assertThat(body.getTimestamp()).isNotNull();
    }
}

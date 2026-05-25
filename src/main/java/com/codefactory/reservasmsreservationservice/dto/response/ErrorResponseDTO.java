package com.codefactory.reservasmsreservationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de error estándar para respuestas de error.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String codigoError; // Código de error customizado (ej: RESERVATION_NOT_FOUND)
    private String message;
    private String path;
    private String detalles; // Detalles adicionales opcionales
}
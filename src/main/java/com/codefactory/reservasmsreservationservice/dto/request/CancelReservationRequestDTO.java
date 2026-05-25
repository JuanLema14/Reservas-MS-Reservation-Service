package com.codefactory.reservasmsreservationservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cancelar una reserva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelReservationRequestDTO {

    @Size(max = 500, message = "Los comentarios de cancelación no pueden exceder 500 caracteres")
    private String comentariosCancelacion;
}
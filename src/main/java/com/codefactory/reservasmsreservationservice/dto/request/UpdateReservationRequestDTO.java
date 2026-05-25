package com.codefactory.reservasmsreservationservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar una reserva existente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationRequestDTO {

    private String fechaHoraInicio; // Nueva fecha y hora

    @Size(max = 500, message = "Los comentarios no pueden exceder 500 caracteres")
    private String comentarios;
}
package com.codefactory.reservasmsreservationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para crear una nueva reserva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequestDTO {

    @NotNull(message = "El ID del servicio es obligatorio")
    private UUID idServicio;

    @NotNull(message = "El ID del empleado es obligatorio")
    private UUID idEmpleado;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private String fechaHoraInicio; // ISO 8601 format: 2025-08-20T10:00:00Z

    @Size(max = 500, message = "Los comentarios no pueden exceder 500 caracteres")
    private String comentarios;

    /**
     * Indica si el cliente prefiere que se le asigne un empleado aleatorio
     * disponible en el horario seleccionado.
     */
    private Boolean preferEmployeeRandom = false;
}
package com.codefactory.reservasmsreservationservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para verificar disponibilidad de un empleado en un horario específico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckAvailabilityRequestDTO {

    private UUID idEmpleado;
    
    private String fechaHoraInicio; // ISO 8601 format
    
    private String fechaHoraFin; // ISO 8601 format
}
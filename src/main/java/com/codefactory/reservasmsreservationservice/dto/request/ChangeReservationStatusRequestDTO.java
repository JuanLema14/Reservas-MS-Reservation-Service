package com.codefactory.reservasmsreservationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para cambiar el estado de una reserva (usado por proveedores/admins).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeReservationStatusRequestDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private String estado;

    private String comentarios;
}
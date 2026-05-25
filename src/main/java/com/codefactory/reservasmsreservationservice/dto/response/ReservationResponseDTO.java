package com.codefactory.reservasmsreservationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una reserva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

    private UUID idReserva;
    private UUID idCliente;
    private String clienteNombre;
    private String clienteEmail;
    private UUID idServicio;
    private String servicioNombre;
    private Integer duracionMinutos;
    private UUID idEmpleado;
    private String empleadoNombre;
    private UUID idProveedor;
    private String proveedorNombre;
    private OffsetDateTime fechaHoraInicio;
    private OffsetDateTime fechaHoraFin;
    private String estado;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaCancelacion;
    private String comentarios;
}
package com.codefactory.reservasmsreservationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una reserva.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO extends RepresentationModel<ReservationResponseDTO> {

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
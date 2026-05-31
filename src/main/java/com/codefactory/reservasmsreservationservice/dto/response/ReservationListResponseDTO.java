package com.codefactory.reservasmsreservationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta paginada para lista de reservas.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationListResponseDTO extends RepresentationModel<ReservationListResponseDTO> {

    private List<ReservationResponseDTO> reservas;
    private int total;
    private int pagina;
    private int tamanioPagina;
    private boolean tieneSiguiente;
}
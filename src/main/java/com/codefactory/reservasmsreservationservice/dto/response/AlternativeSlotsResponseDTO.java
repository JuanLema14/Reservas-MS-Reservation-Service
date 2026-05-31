package com.codefactory.reservasmsreservationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.UUID;

/**
 * DTO para sugerir horarios alternativos cuando hay conflicto.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlternativeSlotsResponseDTO extends RepresentationModel<AlternativeSlotsResponseDTO> {

    private UUID idEmpleado;
    private String empleadoNombre;
    private List<SlotOptionDTO> slotsDisponibles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotOptionDTO {
        private String fechaHoraInicio;
        private String fechaHoraFin;
    }
}
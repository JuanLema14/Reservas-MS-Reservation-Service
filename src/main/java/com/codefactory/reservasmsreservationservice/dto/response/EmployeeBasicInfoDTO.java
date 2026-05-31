package com.codefactory.reservasmsreservationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

/**
 * DTO para información básica de empleado del Schedule Service.
 * Solo contiene los campos necesarios para enriquecer respuestas.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBasicInfoDTO extends RepresentationModel<EmployeeBasicInfoDTO> {

    private UUID id;
    private String fullName;
}
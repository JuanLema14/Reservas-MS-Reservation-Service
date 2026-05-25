package com.codefactory.reservasmsreservationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para información básica de empleado del Schedule Service.
 * Solo contiene los campos necesarios para enriquecer respuestas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBasicInfoDTO {

    private UUID id;
    private String fullName;
}
package com.codefactory.reservasmsreservationservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO local que representa la respuesta de MS-CATALOG-SERVICE al consultar un servicio.
 * Se usa como Anti-Corruption Layer para evitar acoplamiento directo con el modelo del catálogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalServiceDTO {
    private UUID id;
    private String nombreServicio;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private String descripcion;
    private Boolean activo;
    private UUID idProveedor;
    private UUID idCategoria;

    /**
     * Indica si el servicio está activo en el sistema.
     */
    public boolean isActivo() {
        return activo != null && activo;
    }
}
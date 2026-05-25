package com.codefactory.reservasmsreservationservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO local que representa la respuesta de MS-AUTH-SERVICE al consultar un proveedor.
 * Se usa como Anti-Corruption Layer para evitar acoplamiento directo con el modelo de autenticación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProviderDTO {
    private UUID id;
    private String nombreComercial;
    private String email;
    private String telefonoContacto;
    private UUID idCategoria;
    private String direccion;
    private boolean activo;
    private String tipoUsuario;

    /**
     * Indica si el proveedor está activo en el sistema.
     */
    public boolean isActivo() {
        return activo;
    }
}
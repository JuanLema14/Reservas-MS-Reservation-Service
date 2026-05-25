package com.codefactory.reservasmsreservationservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO local que representa la respuesta de MS-AUTH-SERVICE al consultar un cliente.
 * Se usa como Anti-Corruption Layer para evitar acoplamiento directo con el modelo de autenticación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalClientDTO {
    private UUID id;
    private String nombre;
    private String email;
    private String telefono;
    private boolean emailVerificado;
    private boolean activo;
    private String tipoUsuario;

    /**
     * Indica si el cliente está activo en el sistema.
     */
    public boolean isActivo() {
        return activo;
    }
}
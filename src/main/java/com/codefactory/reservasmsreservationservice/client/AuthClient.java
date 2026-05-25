package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.config.FeignConfig;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalClientDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalProviderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Client Feign para comunicarse con el MS-Auth-Service.
 * Se utiliza para validar usuarios, obtener roles y datos de clientes/proveedores.
 */
@FeignClient(
        name = "auth-service",
        url = "${services.auth-service.url}",
        configuration = FeignConfig.class
)
public interface AuthClient {

    /**
     * Obtiene los datos de un cliente por su ID (UUID).
     * Se usa para validar que el cliente existe y obtener sus datos.
     */
    @GetMapping("/api/users/clients/{id}")
    ResponseEntity<ExternalClientDTO> getClientById(@PathVariable("id") UUID id);

    /**
     * Obtiene los datos de un proveedor por su ID (UUID).
     * Se usa para validar que el proveedor existe y obtener sus datos.
     */
    @GetMapping("/api/users/providers/{id}")
    ResponseEntity<ExternalProviderDTO> getProviderById(@PathVariable("id") UUID id);

    /**
     * Obtiene los datos de un usuario por su ID (UUID).
     * Incluye el tipo de usuario para autorización.
     */
    @GetMapping("/api/users/{id}")
    ResponseEntity<ExternalClientDTO> getUserById(@PathVariable("id") UUID id);

    /**
     * Verifica si un usuario existe y está activo.
     */
    @GetMapping("/api/users/{id}/exists")
    ResponseEntity<Boolean> userExists(@PathVariable("id") UUID id);
}
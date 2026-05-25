package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.config.FeignConfig;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalServiceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Client Feign para comunicarse con el MS-Catalog-Service.
 * Se utiliza para validar servicios y obtener información de catálogo.
 */
@FeignClient(
        name = "catalog-service",
        url = "${services.catalog-service.url}",
        configuration = FeignConfig.class
)
public interface CatalogClient {

    /**
     * Obtiene los detalles de un servicio por su ID (UUID).
     * Se usa para validar que el servicio existe, está activo,
     * y obtener duración/precio para la reserva.
     */
    @GetMapping("/api/catalog/services/{id}")
    ResponseEntity<ExternalServiceDTO> getServiceById(@PathVariable("id") UUID id);

    /**
     * Verifica si un servicio está activo y disponible para reservas.
     */
    @GetMapping("/api/catalog/services/{id}/active")
    ResponseEntity<Boolean> isServiceActive(@PathVariable("id") UUID id);
}
package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.dto.external.ExternalServiceDTO;
import com.codefactory.reservasmsreservationservice.exception.ExternalServiceException;
import com.codefactory.reservasmsreservationservice.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Wrapper para el CatalogClient que maneja errores de forma centralizada.
 * Proporciona métodos de conveniencia con manejo de excepciones.
 */
@Component
@RequiredArgsConstructor
public class CatalogClientWrapper {

    private final CatalogClient catalogClient;

    /**
     * Obtiene un servicio o lanza excepción si no existe.
     */
    public ExternalServiceDTO getServiceOrThrow(UUID serviceId) {
        try {
            var response = catalogClient.getServiceById(serviceId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw ExternalServiceException.unavailable("catalog-service");
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Servicio no encontrado con ID: " + serviceId);
        } catch (FeignException e) {
            throw ExternalServiceException.unavailable("catalog-service");
        }
    }

    /**
     * Valida que un servicio existe, está activo y disponible para reservas.
     */
    public void validateService(UUID serviceId) {
        ExternalServiceDTO service = getServiceOrThrow(serviceId);
        if (!service.isActivo()) {
            throw new ResourceNotFoundException("Servicio no está activo: " + serviceId);
        }
    }
}
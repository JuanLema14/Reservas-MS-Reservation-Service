package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.config.CacheConfig;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalServiceDTO;
import com.codefactory.reservasmsreservationservice.exception.ExternalServiceException;
import com.codefactory.reservasmsreservationservice.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Wrapper para el CatalogClient que maneja errores de forma centralizada.
 * Proporciona métodos de conveniencia con manejo de excepciones y caching.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogClientWrapper {

    private final CatalogClient catalogClient;

    /**
     * Obtiene un servicio o lanza excepción si no existe.
     * Resultados cacheados por 5 minutos para evitar llamadas repetidas.
     */
    @Cacheable(value = CacheConfig.SERVICE_CACHE, key = "#serviceId")
    public ExternalServiceDTO getServiceOrThrow(UUID serviceId) {
        try {
            log.debug("Consultando servicio {} en catalog-service (cache miss)", serviceId);
            var response = catalogClient.getServiceById(serviceId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Servicio encontrado: id={}, nombre={}", serviceId, response.getBody().getNombreServicio());
                return response.getBody();
            }
            throw ExternalServiceException.unavailable("catalog-service");
        } catch (FeignException.NotFound e) {
            log.warn("Servicio no encontrado en catalog-service: {}", serviceId);
            throw new ResourceNotFoundException("Servicio no encontrado con ID: " + serviceId);
        } catch (FeignException e) {
            log.error("Error comunicándose con catalog-service: {}", e.getMessage());
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

    /**
     * Invalida cache de servicio cuando se actualiza.
     */
    @CacheEvict(value = CacheConfig.SERVICE_CACHE, key = "#serviceId")
    public void evictServiceCache(UUID serviceId) {
        log.debug("Invalidando cache de servicio: {}", serviceId);
    }
}

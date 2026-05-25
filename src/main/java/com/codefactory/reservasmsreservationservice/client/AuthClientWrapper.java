package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.dto.external.ExternalClientDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalProviderDTO;
import com.codefactory.reservasmsreservationservice.exception.ExternalServiceException;
import com.codefactory.reservasmsreservationservice.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Wrapper para el AuthClient que maneja errores de forma centralizada.
 * Proporciona métodos de conveniencia con manejo de excepciones.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthClientWrapper {

    private final AuthClient authClient;

    /**
     * Obtiene un cliente o lanza excepción si no existe.
     */
    public ExternalClientDTO getClientOrThrow(UUID clientId) {
        try {
            log.debug("Consultando cliente {} en auth-service", clientId);
            var response = authClient.getClientById(clientId);
            log.debug("Respuesta de auth-service: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ExternalClientDTO cliente = response.getBody();
                log.debug("Cliente encontrado: id={}, nombre={}, activo={}", 
                        cliente.getId(), cliente.getNombre(), cliente.isActivo());
                return cliente;
            }
            throw ExternalServiceException.unavailable("auth-service");
        } catch (FeignException.NotFound e) {
            log.warn("Cliente no encontrado en auth-service: {}", clientId);
            throw new ResourceNotFoundException("Cliente no encontrado con ID: " + clientId);
        } catch (FeignException e) {
            log.error("Error comunicándose con auth-service: {}", e.getMessage());
            throw ExternalServiceException.unavailable("auth-service");
        }
    }

    /**
     * Obtiene un proveedor o lanza excepción si no existe.
     */
    public ExternalProviderDTO getProviderOrThrow(UUID providerId) {
        try {
            log.debug("Consultando proveedor {} en auth-service", providerId);
            var response = authClient.getProviderById(providerId);
            log.debug("Respuesta de auth-service: status={}, body={}", response.getStatusCode(), response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw ExternalServiceException.unavailable("auth-service");
        } catch (FeignException.NotFound e) {
            log.warn("Proveedor no encontrado en auth-service: {}", providerId);
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + providerId);
        } catch (FeignException e) {
            log.error("Error comunicándose con auth-service: {}", e.getMessage());
            throw ExternalServiceException.unavailable("auth-service");
        }
    }

    /**
     * Verifica si un usuario existe en el sistema.
     */
    public boolean userExists(UUID userId) {
        try {
            var response = authClient.userExists(userId);
            return response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody());
        } catch (FeignException e) {
            throw ExternalServiceException.unavailable("auth-service");
        }
    }

    /**
     * Valida que un cliente existe y está activo.
     */
    public void validateClient(UUID clientId) {
        ExternalClientDTO client = getClientOrThrow(clientId);
        if (!client.isActivo()) {
            throw new ResourceNotFoundException("Cliente no está activo: " + clientId);
        }
    }

    /**
     * Valida que un proveedor existe y está activo.
     */
    public void validateProvider(UUID providerId) {
        ExternalProviderDTO provider = getProviderOrThrow(providerId);
        if (!provider.isActivo()) {
            throw new ResourceNotFoundException("Proveedor no está activo: " + providerId);
        }
    }
}
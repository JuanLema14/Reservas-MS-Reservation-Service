package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.config.CacheConfig;
import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationBlockRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.EmployeeBasicInfoDTO;
import com.codefactory.reservasmsreservationservice.exception.ExternalServiceException;
import com.codefactory.reservasmsreservationservice.exception.ResourceNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Wrapper para el ScheduleClient que maneja errores de forma centralizada.
 * Proporciona métodos de conveniencia con manejo de excepciones y caching.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleClientWrapper {

    private final ScheduleClient scheduleClient;

    /**
     * Verifica si un empleado está activo.
     */
    @Cacheable(value = CacheConfig.EMPLOYEE_CACHE, key = "'active-' + #employeeId")
    public boolean isEmployeeActive(UUID employeeId) {
        try {
            log.debug("Consultando estado activo de empleado {} (cache miss)", employeeId);
            ResponseEntity<Boolean> response = scheduleClient.isEmployeeActive(employeeId);
            return response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Empleado no encontrado con ID: " + employeeId);
        } catch (FeignException e) {
            throw ExternalServiceException.unavailable("schedule-service");
        }
    }

    /**
     * Obtiene el ID del proveedor asociado a un empleado.
     * Cacheado para evitar llamadas repetidas.
     */
    @Cacheable(value = CacheConfig.EMPLOYEE_CACHE, key = "'provider-' + #employeeId")
    public UUID getEmployeeProviderId(UUID employeeId) {
        try {
            log.debug("Consultando proveedor de empleado {} (cache miss)", employeeId);
            ResponseEntity<UUID> response = scheduleClient.getEmployeeProviderId(employeeId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw ExternalServiceException.unavailable("schedule-service");
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Empleado no encontrado con ID: " + employeeId);
        } catch (FeignException e) {
            throw ExternalServiceException.unavailable("schedule-service");
        }
    }

    /**
     * Verifica si un empleado está disponible en un rango de tiempo específico.
     * NO se cachea porque depende del tiempo exacto.
     */
    public boolean isEmployeeAvailable(UUID employeeId, OffsetDateTime startTime, OffsetDateTime endTime) {
        try {
            ResponseEntity<Boolean> response = scheduleClient.checkEmployeeAvailability(
                    employeeId, startTime, endTime);
            return response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Empleado no encontrado con ID: " + employeeId);
        } catch (FeignException e) {
            throw ExternalServiceException.unavailable("schedule-service");
        }
    }

    /**
     * Crea un bloqueo de horario para una reserva en el Schedule Service.
     * Debe llamarse cuando se crea una nueva reserva.
     */
    public void createReservationBlock(UUID reservationId, UUID employeeId, LocalDate date,
                                       LocalTime startTime, LocalTime endTime) {
        try {
            CreateReservationBlockRequestDTO request = CreateReservationBlockRequestDTO.builder()
                    .reservationId(reservationId)
                    .employeeId(employeeId)
                    .date(date)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            log.info("Creando bloqueo de horario en schedule-service para reserva: {}", reservationId);
            ResponseEntity<Void> response = scheduleClient.createReservationBlock(request);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Bloqueo de horario creado exitosamente para reserva: {}", reservationId);
            } else {
                log.warn("No se pudo crear el bloqueo de horario para reserva: {}, status: {}",
                        reservationId, response.getStatusCode());
            }
        } catch (FeignException e) {
            log.error("Error al crear bloqueo de horario para reserva {}: {}", reservationId, e.getMessage());
            // Don't throw - the reservation was created, just the block failed
        }
    }

    /**
     * Cancela un bloqueo de horario de una reserva en el Schedule Service.
     * Debe llamarse cuando se cancela una reserva, liberando el horario.
     */
    public void cancelReservationBlock(UUID reservationId) {
        try {
            log.info("Cancelando bloqueo de horario en schedule-service para reserva: {}", reservationId);
            ResponseEntity<Void> response = scheduleClient.cancelReservationBlock(reservationId);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Bloqueo de horario cancelado exitosamente para reserva: {}", reservationId);
            } else {
                log.warn("No se pudo cancelar el bloqueo de horario para reserva: {}, status: {}",
                        reservationId, response.getStatusCode());
            }
        } catch (FeignException e) {
            log.error("Error al cancelar bloqueo de horario para reserva {}: {}", reservationId, e.getMessage());
            // Don't throw - the reservation was cancelled, just the block cancel failed
        }
    }

    /**
     * Obtiene información básica de un empleado (nombre).
     * Cacheado para evitar llamadas repetidas.
     */
    @Cacheable(value = CacheConfig.EMPLOYEE_CACHE, key = "'info-' + #employeeId")
    public EmployeeBasicInfoDTO getEmployeeBasicInfo(UUID employeeId) {
        try {
            log.debug("Consultando info de empleado {} (cache miss)", employeeId);
            ResponseEntity<EmployeeBasicInfoDTO> response = scheduleClient.getEmployeeBasicInfo(employeeId);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (FeignException.NotFound e) {
            log.warn("Empleado no encontrado con ID: {}", employeeId);
            return null;
        } catch (FeignException e) {
            log.error("Error al obtener info del empleado {}: {}", employeeId, e.getMessage());
            return null;
        }
    }

    /**
     * Valida que un empleado existe y está activo.
     */
    public void validateEmployee(UUID employeeId) {
        if (!isEmployeeActive(employeeId)) {
            throw new ResourceNotFoundException("Empleado no está activo: " + employeeId);
        }
    }

    /**
     * Valida que la disponibilidad pertenece al empleado correcto.
     */
    public void validateEmployeeBelongsToProvider(UUID employeeId, UUID providerId) {
        UUID actualProviderId = getEmployeeProviderId(employeeId);
        if (!actualProviderId.equals(providerId)) {
            throw new ResourceNotFoundException(
                    "El empleado " + employeeId + " no pertenece al proveedor " + providerId);
        }
    }

    /**
     * Invalida cache de empleado cuando se actualiza.
     */
    @CacheEvict(value = CacheConfig.EMPLOYEE_CACHE, allEntries = true)
    public void evictEmployeeCache(UUID employeeId) {
        log.debug("Invalidando cache de empleado: {}", employeeId);
    }
}
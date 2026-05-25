package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.config.FeignConfig;
import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationBlockRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.EmployeeBasicInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Client Feign para comunicarse con el MS-Schedule-Service.
 * Se utiliza para validar empleados, disponibilidad de horarios y gestionar bloques de reserva.
 */
@FeignClient(
        name = "schedule-service",
        url = "${services.schedule-service.url}",
        configuration = FeignConfig.class
)
public interface ScheduleClient {

    /**
     * Verifica si un empleado existe y está activo.
     */
    @GetMapping("/api/schedule/employees/{id}/active")
    ResponseEntity<Boolean> isEmployeeActive(@PathVariable("id") UUID id);

    /**
     * Obtiene el ID del proveedor asociado a un empleado.
     * Se usa para validar que la reserva pertenece al proveedor correcto.
     */
    @GetMapping("/api/schedule/employees/{id}/provider")
    ResponseEntity<UUID> getEmployeeProviderId(@PathVariable("id") UUID id);

    /**
     * Obtiene información básica de un empleado (nombre).
     * Se usa para enriquecer la respuesta de reservas.
     */
    @GetMapping("/api/schedule/employees/{id}/info")
    ResponseEntity<EmployeeBasicInfoDTO> getEmployeeBasicInfo(@PathVariable("id") UUID id);

    /**
     * Verifica si un empleado está disponible en un rango de tiempo específico.
     */
    @GetMapping("/api/schedule/availability/{employeeId}")
    ResponseEntity<Boolean> checkEmployeeAvailability(
            @PathVariable("employeeId") UUID employeeId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime
    );

    /**
     * Crea un bloqueo de horario para una reserva.
     * Llamado cuando se crea una nueva reserva en el sistema.
     */
    @PostMapping("/api/schedule/schedule-blocks/reservation")
    ResponseEntity<Void> createReservationBlock(@RequestBody CreateReservationBlockRequestDTO request);

    /**
     * Cancela un bloqueo de horario de una reserva.
     * Llamado cuando se cancela una reserva, liberando el horario para otros clientes.
     */
    @DeleteMapping("/api/schedule/schedule-blocks/reservation/{reservationId}")
    ResponseEntity<Void> cancelReservationBlock(@PathVariable("reservationId") UUID reservationId);
}
package com.codefactory.reservasmsreservationservice.controller;

import com.codefactory.reservasmsreservationservice.dto.request.CancelReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.ChangeReservationStatusRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.UpdateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationListResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationResponseDTO;
import com.codefactory.reservasmsreservationservice.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller para gestión de reservas.
 * Endpoints para clientes, proveedores y administradores.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "API para gestión de reservas de servicios")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    // ==================== CREAR RESERVA (CLIENTES) ====================

    @PostMapping
    @Operation(summary = "Crear una nueva reserva",
               description = "Permite a un cliente autenticado crear una reserva para un servicio")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de horarios"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "Horario no disponible")
    })
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReservationRequestDTO request) {
        
        UUID clienteId = extractUserId(userDetails);
        ReservationResponseDTO reserva = reservationService.createReservation(clienteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    // ==================== CONSULTAR RESERVAS ====================

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por ID",
               description = "Devuelve los detalles de una reserva específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @PathVariable UUID id) {
        ReservationResponseDTO reserva = reservationService.getReservationById(id);
        return ResponseEntity.ok(reserva);
    }

    @GetMapping("/my")
    @Operation(summary = "Listar mis reservas",
               description = "Devuelve todas las reservas del cliente autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reservas"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<ReservationListResponseDTO> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Filtrar por estado (PENDIENTE, CONFIRMADA, etc.)")
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        
        UUID clienteId = extractUserId(userDetails);
        ReservationListResponseDTO reservas = reservationService.getReservationsByClient(
                clienteId, estado, pagina, tamanio);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/provider")
    @Operation(summary = "Listar reservas de mis servicios",
               description = "Devuelve todas las reservas asociadas a los servicios del proveedor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reservas"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Solo proveedores o admins")
    })
    @PreAuthorize("hasRole('PROVEEDOR') or hasRole('ADMIN')")
    public ResponseEntity<ReservationListResponseDTO> getProviderReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        
        UUID proveedorId = extractUserId(userDetails);
        ReservationListResponseDTO reservas = reservationService.getReservationsByProvider(
                proveedorId, estado, pagina, tamanio);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar reservas de un empleado",
               description = "Devuelve todas las reservas de un empleado específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reservas"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
    public ResponseEntity<ReservationListResponseDTO> getEmployeeReservations(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        
        ReservationListResponseDTO reservas = reservationService.getReservationsByEmployee(
                employeeId, estado, pagina, tamanio);
        return ResponseEntity.ok(reservas);
    }

    // ==================== ACTUALIZAR RESERVA ====================

    @PutMapping("/{id}")
    @Operation(summary = "Reprogramar/actualizar una reserva",
               description = "Permite al cliente modificar fecha/hora o comentarios de su reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No es dueño de la reserva"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "409", description = "Horario no disponible")
    })
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReservationRequestDTO request) {
        
        UUID clienteId = extractUserId(userDetails);
        ReservationResponseDTO reserva = reservationService.updateReservation(clienteId, id, request);
        return ResponseEntity.ok(reserva);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de reserva",
               description = "Permite a proveedores/admins cambiar el estado (confirmar, completar, no-show)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
    public ResponseEntity<ReservationResponseDTO> changeReservationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeReservationStatusRequestDTO request) {
        
        ReservationResponseDTO reserva = reservationService.changeReservationStatus(id, request);
        return ResponseEntity.ok(reserva);
    }

    // ==================== CANCELAR RESERVA ====================

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar una reserva",
               description = "Permite al cliente cancelar su propia reserva")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada"),
            @ApiResponse(responseCode = "400", description = "La reserva no puede ser cancelada"),
            @ApiResponse(responseCode = "403", description = "No es dueño de la reserva"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelReservationRequestDTO request) {
        
        UUID clienteId = extractUserId(userDetails);
        ReservationResponseDTO reserva = reservationService.cancelReservation(clienteId, id, request);
        return ResponseEntity.ok(reserva);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Extrae el UUID del usuario desde los UserDetails.
     * El UUID se almacena como username en el JWT.
     */
    private UUID extractUserId(UserDetails userDetails) {
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID de usuario inválido en el token");
        }
    }
}
package com.codefactory.reservasmsreservationservice.service;

import com.codefactory.reservasmsreservationservice.dto.request.CancelReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.ChangeReservationStatusRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.UpdateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.AlternativeSlotsResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationListResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationResponseDTO;

import java.util.UUID;

/**
 * Service interface para operaciones de reservas.
 */
public interface ReservationService {

    // ==================== CREAR RESERVA ====================

    /**
     * Crea una nueva reserva para un cliente autenticado.
     *
     * @param clienteId ID del cliente autenticado
     * @param request   Datos de la reserva
     * @return Reserva creada
     */
    ReservationResponseDTO createReservation(UUID clienteId, CreateReservationRequestDTO request);

    // ==================== CONSULTAR RESERVAS ====================

    /**
     * Obtiene una reserva por su ID.
     *
     * @param reservaId ID de la reserva
     * @return Datos de la reserva
     */
    ReservationResponseDTO getReservationById(UUID reservaId);

    /**
     * Lista todas las reservas de un cliente.
     *
     * @param clienteId ID del cliente
     * @param estado    Filtrar por estado (opcional)
     * @param pagina    Número de página
     * @param tamanio  Tamaño de página
     * @return Lista paginada de reservas
     */
    ReservationListResponseDTO getReservationsByClient(UUID clienteId, String estado, int pagina, int tamanio);

    /**
     * Lista todas las reservas de un proveedor (para proveedores y admins).
     *
     * @param proveedorId ID del proveedor
     * @param estado     Filtrar por estado (opcional)
     * @param pagina     Número de página
     * @param tamanio    Tamaño de página
     * @return Lista paginada de reservas
     */
    ReservationListResponseDTO getReservationsByProvider(UUID proveedorId, String estado, int pagina, int tamanio);

    /**
     * Lista todas las reservas de un empleado específico.
     *
     * @param empleadoId ID del empleado
     * @param estado     Filtrar por estado (opcional)
     * @param pagina     Número de página
     * @param tamanio    Tamaño de página
     * @return Lista paginada de reservas
     */
    ReservationListResponseDTO getReservationsByEmployee(UUID empleadoId, String estado, int pagina, int tamanio);

    // ==================== ACTUALIZAR RESERVA ====================

    /**
     * Actualiza una reserva existente (reprogramación).
     * Solo el cliente dueño de la reserva puede actualizarla.
     *
     * @param clienteId  ID del cliente
     * @param reservaId ID de la reserva
     * @param request   Nuevos datos
     * @return Reserva actualizada
     */
    ReservationResponseDTO updateReservation(UUID clienteId, UUID reservaId, UpdateReservationRequestDTO request);

    /**
     * Cambia el estado de una reserva (para proveedores y admins).
     *
     * @param reservaId ID de la reserva
     * @param request  Nuevo estado
     * @return Reserva actualizada
     */
    ReservationResponseDTO changeReservationStatus(UUID reservaId, ChangeReservationStatusRequestDTO request);

    // ==================== CANCELAR RESERVA ====================

    /**
     * Cancela una reserva.
     * Solo el cliente dueño de la reserva puede cancelarla.
     *
     * @param clienteId ID del cliente
     * @param reservaId ID de la reserva
     * @param request  Datos de cancelación (comentarios)
     * @return Reserva cancelada
     */
    ReservationResponseDTO cancelReservation(UUID clienteId, UUID reservaId, CancelReservationRequestDTO request);

    // ==================== DISPONIBILIDAD ====================

    /**
     * Verifica si un horario está disponible para un empleado.
     *
     * @param idEmpleado       ID del empleado
     * @param fechaHoraInicio Fecha y hora de inicio
     * @param fechaHoraFin     Fecha y hora de fin
     * @return true si está disponible
     */
    boolean checkAvailability(UUID idEmpleado, String fechaHoraInicio, String fechaHoraFin);

    /**
     * Obtiene horarios alternativos disponibles.
     *
     * @param idServicio       ID del servicio
     * @param idEmpleado       ID del empleado (opcional si se quiere cualquier empleado)
     * @param fechaHoraDeseada Fecha y hora deseada
     * @return Lista de horarios alternativos
     */
    AlternativeSlotsResponseDTO getAlternativeSlots(UUID idServicio, UUID idEmpleado, String fechaHoraDeseada);
}
package com.codefactory.reservasmsreservationservice.repository;

import com.codefactory.reservasmsreservationservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository para la entidad Reservation.
 * Proporciona operaciones CRUD básicas y consultas específicas para reservas.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    /**
     * Busca todas las reservas de un cliente.
     */
    List<Reservation> findByIdClienteOrderByFechaHoraInicioDesc(UUID idCliente);

    /**
     * Busca todas las reservas de un empleado.
     */
    List<Reservation> findByIdEmpleadoOrderByFechaHoraInicioAsc(UUID idEmpleado);

    /**
     * Busca reservas por estado para un empleado específico.
     */
    List<Reservation> findByIdEmpleadoAndEstado(UUID idEmpleado, Reservation.ReservationStatus estado);

    /**
     * Busca todas las reservas de un proveedor.
     */
    List<Reservation> findByIdProveedorOrderByFechaHoraInicioDesc(UUID idProveedor);

    /**
     * Busca reservas por estado para un cliente específico.
     */
    List<Reservation> findByIdClienteAndEstado(UUID idCliente, Reservation.ReservationStatus estado);

    /**
     * Busca reservas por estado para un proveedor específico.
     */
    List<Reservation> findByIdProveedorAndEstado(UUID idProveedor, Reservation.ReservationStatus estado);

    /**
     * Busca reservas activas para un empleado en un rango de fechas.
     */
    @Query("SELECT r FROM Reservation r WHERE r.idEmpleado = :idEmpleado " +
           "AND r.fechaHoraInicio >= :fechaInicio AND r.fechaHoraFin <= :fechaFin " +
           "AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_PROGRESO') " +
           "ORDER BY r.fechaHoraInicio ASC")
    List<Reservation> findActiveReservationsByEmployeeAndDateRange(
            @Param("idEmpleado") UUID idEmpleado,
            @Param("fechaInicio") OffsetDateTime fechaInicio,
            @Param("fechaFin") OffsetDateTime fechaFin);

    /**
     * Verifica si existe un conflicto de horarios para un empleado.
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.idEmpleado = :idEmpleado " +
           "AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_PROGRESO') " +
           "AND r.fechaHoraInicio < :fechaFin AND r.fechaHoraFin > :fechaInicio")
    boolean existsConflictingReservation(
            @Param("idEmpleado") UUID idEmpleado,
            @Param("fechaInicio") OffsetDateTime fechaInicio,
            @Param("fechaFin") OffsetDateTime fechaFin);

    /**
     * Busca reservas por rango de fechas para un proveedor.
     */
    List<Reservation> findByIdProveedorAndFechaHoraInicioBetween(
            UUID idProveedor, OffsetDateTime fechaInicio, OffsetDateTime fechaFin);

    /**
     * Cuenta las reservas por estado para un proveedor.
     */
    long countByIdProveedorAndEstado(UUID idProveedor, Reservation.ReservationStatus estado);
}
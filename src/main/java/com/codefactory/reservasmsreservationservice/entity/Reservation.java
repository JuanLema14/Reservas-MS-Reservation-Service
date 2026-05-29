package com.codefactory.reservasmsreservationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity que representa una reserva en el microservicio de reservas.
 * Contiene información sobre citas agendadas por clientes con proveedores.
 */
@Entity
@Table(name = "reserva", indexes = {
        @Index(name = "idx_reserva_cliente_fecha", columnList = "id_cliente, fecha_hora_inicio DESC"),
        @Index(name = "idx_reserva_proveedor_fecha", columnList = "id_proveedor, fecha_hora_inicio DESC"),
        @Index(name = "idx_reserva_empleado_fecha", columnList = "id_empleado, fecha_hora_inicio DESC"),
        @Index(name = "idx_reserva_estado", columnList = "estado"),
        @Index(name = "idx_reserva_cliente_estado", columnList = "id_cliente, estado"),
        @Index(name = "idx_reserva_proveedor_estado", columnList = "id_proveedor, estado")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_reserva", updatable = false, nullable = false)
    private UUID idReserva;

    @Column(name = "id_cliente", nullable = false)
    private UUID idCliente;

    @Column(name = "id_servicio", nullable = false)
    private UUID idServicio;

    @Column(name = "id_empleado", nullable = false)
    private UUID idEmpleado;

    @Column(name = "id_proveedor", nullable = false)
    private UUID idProveedor;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private OffsetDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private OffsetDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus estado = ReservationStatus.PENDIENTE;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_cancelacion")
    private OffsetDateTime fechaCancelacion;

    @Column(name = "comentarios", length = 500)
    private String comentarios;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Enum para los estados posibles de una reserva.
     */
    public enum ReservationStatus {
        PENDIENTE,
        CONFIRMADA,
        EN_PROGRESO,
        COMPLETADA,
        CANCELADA,
        NO_SHOW
    }

    /**
     * Verifica si la reserva puede ser cancelada.
     */
    public boolean canBeCancelled() {
        return estado == ReservationStatus.PENDIENTE || estado == ReservationStatus.CONFIRMADA;
    }

    /**
     * Verifica si la reserva está activa (no completada ni cancelada).
     */
    public boolean isActive() {
        return estado == ReservationStatus.PENDIENTE ||
               estado == ReservationStatus.CONFIRMADA ||
               estado == ReservationStatus.EN_PROGRESO;
    }
}
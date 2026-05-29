package com.codefactory.reservasmsreservationservice.repository;

import com.codefactory.reservasmsreservationservice.entity.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para ReservationRepository.
 * Utiliza @DataJpaTest con H2 in-memory database.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("MS-Reservation - ReservationRepository (Integration)")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID clienteId;
    private UUID empleadoId;
    private UUID proveedorId;
    private UUID servicioId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        empleadoId = UUID.randomUUID();
        proveedorId = UUID.randomUUID();
        servicioId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar reserva con todos los campos")
        void save_NewReservation_SavesAllFields() {
            // Given
            OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            Reservation reserva = createReservation(clienteId, empleadoId, proveedorId, servicioId,
                    ahora, Reservation.ReservationStatus.CONFIRMADA);

            // When
            Reservation saved = reservationRepository.save(reserva);
            entityManager.flush();

            // Then
            assertThat(saved.getIdReserva()).isNotNull();
            assertThat(saved.getIdCliente()).isEqualTo(clienteId);
            assertThat(saved.getEstado()).isEqualTo(Reservation.ReservationStatus.CONFIRMADA);
            assertThat(saved.getFechaCreacion()).isNotNull();
        }

        @Test
        @DisplayName("Debe establecer estado PENDIENTE por defecto")
        void save_SetsDefaultStatus() {
            // Given
            OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            Reservation reserva = createReservation(clienteId, empleadoId, proveedorId, servicioId,
                    ahora, null);

            // When
            Reservation saved = reservationRepository.save(reserva);
            entityManager.flush();

            // Then
            assertThat(saved.getEstado()).isEqualTo(Reservation.ReservationStatus.PENDIENTE);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar reserva por ID")
        void findById_ExistingReservation_ReturnsReservation() {
            // Given
            Reservation reserva = createAndPersistReservation();
            UUID reservaId = reserva.getIdReserva();

            // When
            Optional<Reservation> found = reservationRepository.findById(reservaId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getIdCliente()).isEqualTo(clienteId);
        }

        @Test
        @DisplayName("Debe retornar empty para ID no existente")
        void findById_NonExistingId_ReturnsEmpty() {
            // When
            Optional<Reservation> found = reservationRepository.findById(UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdClienteOrderByFechaHoraInicioDesc")
    class FindByClienteTests {

        @Test
        @DisplayName("Debe retornar reservas de un cliente ordenadas por fecha")
        void findByIdClienteOrderByFechaHoraInicioDesc_ReturnsReservations() {
            // Given
            OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            OffsetDateTime pasadoManana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(2);
            
            createAndPersistReservationWithTime(clienteId, empleadoId, proveedorId, servicioId, pasadoManana, Reservation.ReservationStatus.CONFIRMADA);
            createAndPersistReservationWithTime(clienteId, empleadoId, proveedorId, servicioId, manana, Reservation.ReservationStatus.PENDIENTE);

            // When
            List<Reservation> reservas = reservationRepository.findByIdClienteOrderByFechaHoraInicioDesc(clienteId);

            // Then
            assertThat(reservas).hasSize(2);
            assertThat(reservas.get(0).getFechaHoraInicio()).isAfterOrEqualTo(reservas.get(1).getFechaHoraInicio());
        }
    }

    @Nested
    @DisplayName("findByIdEmpleadoOrderByFechaHoraInicioAsc")
    class FindByEmpleadoTests {

        @Test
        @DisplayName("Debe retornar reservas de un empleado ordenadas por fecha")
        void findByIdEmpleadoOrderByFechaHoraInicioAsc_ReturnsReservations() {
            // Given
            OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            OffsetDateTime pasadoManana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(2);
            
            createAndPersistReservationWithTime(clienteId, empleadoId, proveedorId, servicioId, manana, Reservation.ReservationStatus.CONFIRMADA);
            createAndPersistReservationWithTime(UUID.randomUUID(), empleadoId, proveedorId, servicioId, pasadoManana, Reservation.ReservationStatus.PENDIENTE);

            // When
            List<Reservation> reservas = reservationRepository.findByIdEmpleadoOrderByFechaHoraInicioAsc(empleadoId);

            // Then
            assertThat(reservas).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByIdEmpleadoAndEstado")
    class FindByEmpleadoAndEstadoTests {

        @Test
        @DisplayName("Debe filtrar reservas por estado")
        void findByIdEmpleadoAndEstado_ReturnsFilteredReservations() {
            // Given
            OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            
            createAndPersistReservationWithTime(clienteId, empleadoId, proveedorId, servicioId, manana, Reservation.ReservationStatus.CONFIRMADA);
            createAndPersistReservationWithTime(UUID.randomUUID(), empleadoId, proveedorId, servicioId, manana.plusHours(1), Reservation.ReservationStatus.CANCELADA);

            // When
            List<Reservation> reservas = reservationRepository.findByIdEmpleadoAndEstado(
                    empleadoId, Reservation.ReservationStatus.CONFIRMADA);

            // Then
            assertThat(reservas).hasSize(1);
            assertThat(reservas.get(0).getEstado()).isEqualTo(Reservation.ReservationStatus.CONFIRMADA);
        }
    }

    @Nested
    @DisplayName("findByIdProveedorOrderByFechaHoraInicioDesc")
    class FindByProveedorTests {

        @Test
        @DisplayName("Debe retornar reservas de un proveedor")
        void findByIdProveedorOrderByFechaHoraInicioDesc_ReturnsReservations() {
            // Given
            createAndPersistReservation();
            createAndPersistReservation();

            // When
            List<Reservation> reservas = reservationRepository.findByIdProveedorOrderByFechaHoraInicioDesc(proveedorId);

            // Then
            assertThat(reservas).hasSize(2);
            assertThat(reservas).allMatch(r -> r.getIdProveedor().equals(proveedorId));
        }
    }

    @Nested
    @DisplayName("existsConflictingReservation")
    class ConflictTests {

        @Test
        @DisplayName("Debe retornar true cuando hay conflicto")
        void existsConflictingReservation_HasConflict_ReturnsTrue() {
            // Given
            Reservation existing = createAndPersistReservation();
            OffsetDateTime conflictingStart = existing.getFechaHoraInicio().plusMinutes(30);
            OffsetDateTime conflictingEnd = conflictingStart.plusHours(1);

            // When
            boolean hasConflict = reservationRepository.existsConflictingReservation(
                    empleadoId, conflictingStart, conflictingEnd);

            // Then
            assertThat(hasConflict).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando no hay conflicto")
        void existsConflictingReservation_NoConflict_ReturnsFalse() {
            // Given
            Reservation existing = createAndPersistReservation();
            OffsetDateTime differentTime = existing.getFechaHoraFin().plusHours(2);
            OffsetDateTime endTime = differentTime.plusHours(1);

            // When
            boolean hasConflict = reservationRepository.existsConflictingReservation(
                    empleadoId, differentTime, endTime);

            // Then
            assertThat(hasConflict).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false para reserva cancelada")
        void existsConflictingReservation_CancelledReservation_ReturnsFalse() {
            // Given - reserva cancelada no debe contar como conflicto
            OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            Reservation cancelled = createReservation(clienteId, empleadoId, proveedorId, servicioId, 
                    manana, Reservation.ReservationStatus.CANCELADA);
            entityManager.persist(cancelled);
            entityManager.flush();

            // When
            boolean hasConflict = reservationRepository.existsConflictingReservation(
                    empleadoId, manana, manana.plusHours(1));

            // Then
            assertThat(hasConflict).isFalse();
        }
    }

    @Nested
    @DisplayName("findActiveReservationsByEmployeeAndDateRange")
    class ActiveReservationsTests {

        @Test
        @DisplayName("Debe retornar solo reservas activas en rango")
        void findActiveReservationsByEmployeeAndDateRange_ReturnsActiveOnly() {
            // Given
            OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            OffsetDateTime pasadoManana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(2);
            
            createAndPersistReservationWithTime(clienteId, empleadoId, proveedorId, servicioId, manana, Reservation.ReservationStatus.CONFIRMADA);
            createAndPersistReservationWithTime(UUID.randomUUID(), empleadoId, proveedorId, servicioId, manana, Reservation.ReservationStatus.CANCELADA);

            // When
            List<Reservation> reservas = reservationRepository.findActiveReservationsByEmployeeAndDateRange(
                    empleadoId, manana.minusHours(1), pasadoManana.plusHours(1));

            // Then
            assertThat(reservas).hasSize(1);
            assertThat(reservas.get(0).getEstado()).isEqualTo(Reservation.ReservationStatus.CONFIRMADA);
        }
    }

    @Nested
    @DisplayName("countByIdProveedorAndEstado")
    class CountTests {

        @Test
        @DisplayName("Debe contar reservas por estado")
        void countByIdProveedorAndEstado_ReturnsCorrectCount() {
            // Given
            OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            
            createAndPersistReservationWithTime(clienteId, empleadoId, proveedorId, servicioId, manana, Reservation.ReservationStatus.CONFIRMADA);
            createAndPersistReservationWithTime(UUID.randomUUID(), empleadoId, proveedorId, servicioId, manana.plusHours(1), Reservation.ReservationStatus.CONFIRMADA);
            createAndPersistReservationWithTime(UUID.randomUUID(), empleadoId, proveedorId, servicioId, manana.plusHours(2), Reservation.ReservationStatus.CANCELADA);

            // When
            long count = reservationRepository.countByIdProveedorAndEstado(
                    proveedorId, Reservation.ReservationStatus.CONFIRMADA);

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    // Helper methods
    private Reservation createAndPersistReservation() {
        OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        return createAndPersistReservationWithTime(clienteId, empleadoId, proveedorId, servicioId, manana, Reservation.ReservationStatus.CONFIRMADA);
    }

    private Reservation createAndPersistReservationWithTime(UUID cliId, UUID empId, UUID provId, UUID servId, 
                                                            OffsetDateTime fechaInicio, Reservation.ReservationStatus estado) {
        Reservation reserva = createReservation(cliId, empId, provId, servId, fechaInicio, estado);
        return entityManager.persist(reserva);
    }

    private Reservation createReservation(UUID cliId, UUID empId, UUID provId, UUID servId,
                                          OffsetDateTime fechaInicio, Reservation.ReservationStatus estado) {
        return Reservation.builder()
                .idCliente(cliId)
                .idEmpleado(empId)
                .idProveedor(provId)
                .idServicio(servId)
                .fechaHoraInicio(fechaInicio)
                .fechaHoraFin(fechaInicio.plusHours(1))
                .estado(estado != null ? estado : Reservation.ReservationStatus.PENDIENTE)
                .comentarios("Test reservation")
                .build();
    }
}
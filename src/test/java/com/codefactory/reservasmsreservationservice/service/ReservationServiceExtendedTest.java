package com.codefactory.reservasmsreservationservice.service;

import com.codefactory.reservasmsreservationservice.client.AuthClientWrapper;
import com.codefactory.reservasmsreservationservice.client.CatalogClientWrapper;
import com.codefactory.reservasmsreservationservice.client.ScheduleClientWrapper;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalClientDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalProviderDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalServiceDTO;
import com.codefactory.reservasmsreservationservice.dto.request.ChangeReservationStatusRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.UpdateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.AlternativeSlotsResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.EmployeeBasicInfoDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationListResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationResponseDTO;
import com.codefactory.reservasmsreservationservice.entity.Reservation;
import com.codefactory.reservasmsreservationservice.exception.*;
import com.codefactory.reservasmsreservationservice.mapper.ReservationMapper;
import com.codefactory.reservasmsreservationservice.repository.ReservationRepository;
import com.codefactory.reservasmsreservationservice.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Extended unit tests for ReservationServiceImpl to increase coverage above 50%.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Reservation - ReservationServiceImpl (Extended)")
class ReservationServiceExtendedTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationMapper reservationMapper;
    @Mock private AuthClientWrapper authClientWrapper;
    @Mock private CatalogClientWrapper catalogClientWrapper;
    @Mock private ScheduleClientWrapper scheduleClientWrapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private UUID clienteId, empleadoId, proveedorId, servicioId, reservaId;
    private Reservation reserva;
    private ExternalClientDTO cliente;
    private ExternalServiceDTO servicio;
    private ExternalProviderDTO proveedor;
    private EmployeeBasicInfoDTO employeeInfo;

    @BeforeEach
    void setUp() {
        clienteId   = UUID.randomUUID();
        empleadoId  = UUID.randomUUID();
        proveedorId = UUID.randomUUID();
        servicioId  = UUID.randomUUID();
        reservaId   = UUID.randomUUID();

        OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        reserva = Reservation.builder()
                .idReserva(reservaId)
                .idCliente(clienteId)
                .idEmpleado(empleadoId)
                .idProveedor(proveedorId)
                .idServicio(servicioId)
                .fechaHoraInicio(manana)
                .fechaHoraFin(manana.plusHours(1))
                .estado(Reservation.ReservationStatus.CONFIRMADA)
                .comentarios("Test")
                .build();

        cliente = ExternalClientDTO.builder()
                .id(clienteId).nombre("Ana García").email("ana@test.com").activo(true).build();

        servicio = ExternalServiceDTO.builder()
                .id(servicioId).nombreServicio("Corte de cabello").activo(true).duracionMinutos(60).build();

        proveedor = ExternalProviderDTO.builder()
                .id(proveedorId).nombreComercial("Salón Glamour").activo(true).build();

        employeeInfo = EmployeeBasicInfoDTO.builder()
                .id(empleadoId).fullName ("Carlos López").build();
    }

    // ==================== UpdateReservation ====================

    @Nested
    @DisplayName("updateReservation")
    class UpdateReservationTests {

        @Test
        @DisplayName("Debe actualizar fecha exitosamente")
        void updateReservation_ValidNewDate_UpdatesReservation() {
            String nuevaFecha = OffsetDateTime.now(ZoneOffset.UTC).plusDays(2).toString();
            UpdateReservationRequestDTO request = UpdateReservationRequestDTO.builder()
                    .fechaHoraInicio(nuevaFecha)
                    .comentarios("Nuevo comentario")
                    .build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(reservationRepository.existsConflictingReservation(eq(empleadoId), any(), any())).thenReturn(false);
            when(reservationRepository.save(any())).thenReturn(reserva);
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationResponseDTO result = reservationService.updateReservation(clienteId, reservaId, request);

            assertThat(result).isNotNull();
            verify(reservationRepository).save(any());
        }

        @Test
        @DisplayName("Debe actualizar solo comentarios sin cambio de fecha")
        void updateReservation_OnlyComments_UpdatesComments() {
            UpdateReservationRequestDTO request = UpdateReservationRequestDTO.builder()
                    .comentarios("Solo cambio comentario")
                    .build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
            when(reservationRepository.save(any())).thenReturn(reserva);
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationResponseDTO result = reservationService.updateReservation(clienteId, reservaId, request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void updateReservation_NotOwner_ThrowsException() {
            UUID otroCliente = UUID.randomUUID();
            UpdateReservationRequestDTO request = UpdateReservationRequestDTO.builder().build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> reservationService.updateReservation(otroCliente, reservaId, request))
                    .isInstanceOf(ReservationAccessDeniedException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si reserva no está activa")
        void updateReservation_NotActive_ThrowsException() {
            reserva.setEstado(Reservation.ReservationStatus.COMPLETADA);
            UpdateReservationRequestDTO request = UpdateReservationRequestDTO.builder().build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> reservationService.updateReservation(clienteId, reservaId, request))
                    .isInstanceOf(InvalidReservationStateException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si nuevo horario tiene conflicto")
        void updateReservation_ConflictingSlot_ThrowsException() {
            String nuevaFecha = OffsetDateTime.now(ZoneOffset.UTC).plusDays(2).toString();
            UpdateReservationRequestDTO request = UpdateReservationRequestDTO.builder()
                    .fechaHoraInicio(nuevaFecha).build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(reservationRepository.existsConflictingReservation(eq(empleadoId), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> reservationService.updateReservation(clienteId, reservaId, request))
                    .isInstanceOf(ReservationConflictException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si reserva no existe")
        void updateReservation_NotFound_ThrowsException() {
            when(reservationRepository.findById(reservaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.updateReservation(clienteId, reservaId,
                    UpdateReservationRequestDTO.builder().build()))
                    .isInstanceOf(ReservationNotFoundException.class);
        }
    }

    // ==================== getReservationsByProvider ====================

    @Nested
    @DisplayName("getReservationsByProvider")
    class GetReservationsByProviderTests {

        @Test
        @DisplayName("Debe retornar reservas del proveedor sin filtro de estado")
        void getReservationsByProvider_NoFilter_ReturnsAll() {
            when(reservationRepository.findByIdProveedorOrderByFechaHoraInicioDesc(proveedorId))
                    .thenReturn(List.of(reserva));
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationListResponseDTO result = reservationService.getReservationsByProvider(proveedorId, null, 0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(1);
        }

        @Test
        @DisplayName("Debe filtrar por estado CONFIRMADA")
        void getReservationsByProvider_WithEstado_FiltersCorrectly() {
            when(reservationRepository.findByIdProveedorAndEstado(proveedorId, Reservation.ReservationStatus.CONFIRMADA))
                    .thenReturn(List.of(reserva));
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationListResponseDTO result = reservationService.getReservationsByProvider(proveedorId, "CONFIRMADA", 0, 10);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Debe lanzar excepción si estado es inválido")
        void getReservationsByProvider_InvalidEstado_ThrowsException() {
            assertThatThrownBy(() ->
                    reservationService.getReservationsByProvider(proveedorId, "ESTADO_INVALIDO", 0, 10))
                    .isInstanceOf(ValidationException.class);
        }
    }

    // ==================== getReservationsByEmployee ====================

    @Nested
    @DisplayName("getReservationsByEmployee")
    class GetReservationsByEmployeeTests {

        @Test
        @DisplayName("Debe retornar reservas del empleado sin filtro")
        void getReservationsByEmployee_NoFilter_ReturnsAll() {
            when(reservationRepository.findByIdEmpleadoOrderByFechaHoraInicioAsc(empleadoId))
                    .thenReturn(List.of(reserva));
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationListResponseDTO result = reservationService.getReservationsByEmployee(empleadoId, null, 0, 10);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Debe filtrar empleado por estado")
        void getReservationsByEmployee_WithEstado_FiltersCorrectly() {
            when(reservationRepository.findByIdEmpleadoAndEstado(empleadoId, Reservation.ReservationStatus.PENDIENTE))
                    .thenReturn(List.of());

            ReservationListResponseDTO result = reservationService.getReservationsByEmployee(empleadoId, "PENDIENTE", 0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(0);
        }
    }

    // ==================== getAlternativeSlots ====================

    @Nested
    @DisplayName("getAlternativeSlots")
    class GetAlternativeSlotsTests {

        @Test
        @DisplayName("Debe retornar slots alternativos vacíos")
        void getAlternativeSlots_ReturnsEmptySlots() {
            String fechaDeseada = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString();

            AlternativeSlotsResponseDTO result = reservationService.getAlternativeSlots(servicioId, empleadoId, fechaDeseada);

            assertThat(result).isNotNull();
            assertThat(result.getSlotsDisponibles()).isEmpty();
            assertThat(result.getIdEmpleado()).isEqualTo(empleadoId);
        }

        @Test
        @DisplayName("Debe retornar slots con idEmpleado null")
        void getAlternativeSlots_NullEmployee_ReturnsSlots() {
            String fechaDeseada = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString();

            AlternativeSlotsResponseDTO result = reservationService.getAlternativeSlots(servicioId, null, fechaDeseada);

            assertThat(result).isNotNull();
        }
    }

    // ==================== checkAvailability edge cases ====================

    @Nested
    @DisplayName("checkAvailability - edge cases")
    class CheckAvailabilityEdgeCasesTests {

        @Test
        @DisplayName("Debe lanzar excepción con formato de fecha inválido - inicio")
        void checkAvailability_InvalidDateFormat_ThrowsException() {
            assertThatThrownBy(() ->
                    reservationService.checkAvailability(empleadoId, "fecha-invalida", "2025-08-20T10:00:00Z"))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción con formato de fecha inválido - fin")
        void checkAvailability_InvalidEndDate_ThrowsException() {
            String inicio = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString();
            assertThatThrownBy(() ->
                    reservationService.checkAvailability(empleadoId, inicio, "fecha-invalida"))
                    .isInstanceOf(ValidationException.class);
        }
    }

    // ==================== changeReservationStatus - estado con comentarios ====================

    @Nested
    @DisplayName("changeReservationStatus - additional cases")
    class ChangeStatusAdditionalTests {

        @Test
        @DisplayName("Debe cambiar estado con comentarios adicionales")
        void changeReservationStatus_WithComments_UpdatesCorrectly() {
            ChangeReservationStatusRequestDTO request = ChangeReservationStatusRequestDTO.builder()
                    .estado("COMPLETADA")
                    .comentarios("Servicio completado satisfactoriamente")
                    .build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
            when(reservationRepository.save(any())).thenReturn(reserva);
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationResponseDTO result = reservationService.changeReservationStatus(reservaId, request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Debe lanzar excepción si estado no es válido")
        void changeReservationStatus_InvalidStatus_ThrowsException() {
            ChangeReservationStatusRequestDTO request = ChangeReservationStatusRequestDTO.builder()
                    .estado("ESTADO_INEXISTENTE")
                    .build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> reservationService.changeReservationStatus(reservaId, request))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("No debe cambiar estado de reserva NO_SHOW")
        void changeReservationStatus_FromNoShow_ThrowsException() {
            reserva.setEstado(Reservation.ReservationStatus.NO_SHOW);
            ChangeReservationStatusRequestDTO request = ChangeReservationStatusRequestDTO.builder()
                    .estado("CONFIRMADA").build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> reservationService.changeReservationStatus(reservaId, request))
                    .isInstanceOf(InvalidReservationStateException.class);
        }

        @Test
        @DisplayName("No debe cambiar estado de reserva COMPLETADA")
        void changeReservationStatus_FromCompletada_ThrowsException() {
            reserva.setEstado(Reservation.ReservationStatus.COMPLETADA);
            ChangeReservationStatusRequestDTO request = ChangeReservationStatusRequestDTO.builder()
                    .estado("CONFIRMADA").build();

            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

            assertThatThrownBy(() -> reservationService.changeReservationStatus(reservaId, request))
                    .isInstanceOf(InvalidReservationStateException.class);
        }
    }

    // ==================== cancelReservation - with null request ====================

    @Nested
    @DisplayName("cancelReservation - additional cases")
    class CancelAdditionalTests {

        @Test
        @DisplayName("Debe cancelar correctamente cuando hay bloqueador de schedule que falla")
        void cancelReservation_ScheduleBlockFails_StillCancels() {
            when(reservationRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
            when(reservationRepository.save(any())).thenReturn(reserva);
            doThrow(new RuntimeException("Schedule service down"))
                    .when(scheduleClientWrapper).cancelReservationBlock(any());
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationResponseDTO result = reservationService.cancelReservation(clienteId, reservaId, null);

            assertThat(result).isNotNull();
        }
    }

    // ==================== Pagination edge cases ====================

    @Nested
    @DisplayName("Pagination")
    class PaginationTests {

        @Test
        @DisplayName("Debe paginar correctamente con múltiples reservas")
        void getReservationsByClient_Paginated_ReturnsCorrectPage() {
            Reservation r2 = Reservation.builder()
                    .idReserva(UUID.randomUUID()).idCliente(clienteId)
                    .idEmpleado(empleadoId).idProveedor(proveedorId).idServicio(servicioId)
                    .fechaHoraInicio(OffsetDateTime.now().plusDays(2))
                    .fechaHoraFin(OffsetDateTime.now().plusDays(2).plusHours(1))
                    .estado(Reservation.ReservationStatus.PENDIENTE)
                    .build();

            when(reservationRepository.findByIdClienteOrderByFechaHoraInicioDesc(clienteId))
                    .thenReturn(List.of(reserva, r2));
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationListResponseDTO result = reservationService.getReservationsByClient(clienteId, null, 0, 1);

            assertThat(result.getTotal()).isEqualTo(2);
            assertThat(result.isTieneSiguiente()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar tieneSiguiente=false en última página")
        void getReservationsByClient_LastPage_NoNext() {
            when(reservationRepository.findByIdClienteOrderByFechaHoraInicioDesc(clienteId))
                    .thenReturn(List.of(reserva));
            when(authClientWrapper.getClientOrThrow(clienteId)).thenReturn(cliente);
            when(authClientWrapper.getProviderOrThrow(proveedorId)).thenReturn(proveedor);
            when(catalogClientWrapper.getServiceOrThrow(servicioId)).thenReturn(servicio);
            when(scheduleClientWrapper.getEmployeeBasicInfo(empleadoId)).thenReturn(employeeInfo);
            when(reservationMapper.toResponseDTO(any(), any(), any(), any(), any()))
                    .thenReturn(ReservationResponseDTO.builder().idReserva(reservaId).build());

            ReservationListResponseDTO result = reservationService.getReservationsByClient(clienteId, null, 0, 10);

            assertThat(result.isTieneSiguiente()).isFalse();
        }
    }
}

package com.codefactory.reservasmsreservationservice.controller;

import com.codefactory.reservasmsreservationservice.dto.request.CancelReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.ChangeReservationStatusRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.UpdateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationListResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationResponseDTO;
import com.codefactory.reservasmsreservationservice.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationController using @ExtendWith(MockitoExtension).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Reservation - ReservationController (Unit)")
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationController reservationController;

    private UUID clienteId;
    private UUID reservaId;
    private UUID proveedorId;
    private UUID empleadoId;
    private UUID servicioId;
    private ReservationResponseDTO reservaResponse;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        reservaId = UUID.randomUUID();
        proveedorId = UUID.randomUUID();
        empleadoId = UUID.randomUUID();
        servicioId = UUID.randomUUID();

        reservaResponse = ReservationResponseDTO.builder()
                .idReserva(reservaId)
                .idCliente(clienteId)
                .idEmpleado(empleadoId)
                .idProveedor(proveedorId)
                .idServicio(servicioId)
                .estado("CONFIRMADA")
                .build();

        userDetails = User.builder()
                .username(clienteId.toString())
                .password("password")
                .authorities("ROLE_CLIENTE")
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("createReservation")
    class CreateReservationTests {

        @Test
        @DisplayName("Debe crear reserva y retornar CREATED")
        void createReservation_ReturnsCreated() {
            // Given
            OffsetDateTime manana = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
            CreateReservationRequestDTO request = CreateReservationRequestDTO.builder()
                    .idServicio(servicioId)
                    .idEmpleado(empleadoId)
                    .fechaHoraInicio(manana.toString())
                    .build();

            when(reservationService.createReservation(eq(clienteId), any())).thenReturn(reservaResponse);

            // When
            ResponseEntity<ReservationResponseDTO> response = reservationController.createReservation(userDetails, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getReservationById")
    class GetReservationByIdTests {

        @Test
        @DisplayName("Debe obtener reserva por ID y retornar OK")
        void getReservationById_ReturnsOk() {
            // Given
            when(reservationService.getReservationById(reservaId)).thenReturn(reservaResponse);

            // When
            ResponseEntity<ReservationResponseDTO> response = reservationController.getReservationById(reservaId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getIdReserva()).isEqualTo(reservaId);
        }
    }

    @Nested
    @DisplayName("getMyReservations")
    class GetMyReservationsTests {

        @Test
        @DisplayName("Debe obtener reservas del cliente y retornar OK")
        void getMyReservations_ReturnsOk() {
            // Given
            ReservationListResponseDTO listResponse = ReservationListResponseDTO.builder()
                    .reservas(List.of(reservaResponse))
                    .total(1)
                    .pagina(0)
                    .tamanioPagina(10)
                    .tieneSiguiente(false)
                    .build();

            when(reservationService.getReservationsByClient(eq(clienteId), any(), anyInt(), anyInt()))
                    .thenReturn(listResponse);

            // When
            ResponseEntity<ReservationListResponseDTO> response = 
                    reservationController.getMyReservations(userDetails, null, 0, 10);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotal()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getProviderReservations")
    class GetProviderReservationsTests {

        @Test
        @DisplayName("Debe obtener reservas del proveedor y retornar OK")
        void getProviderReservations_ReturnsOk() {
            // Given
            ReservationListResponseDTO listResponse = ReservationListResponseDTO.builder()
                    .reservas(List.of(reservaResponse))
                    .total(1)
                    .build();

            // Use a UserDetails with proveedorId as username to match controller's extractUserId()
            UserDetails providerUserDetails = User.builder()
                    .username(proveedorId.toString())
                    .password("password")
                    .authorities("ROLE_PROVEEDOR")
                    .build();

            when(reservationService.getReservationsByProvider(eq(proveedorId), any(), anyInt(), anyInt()))
                    .thenReturn(listResponse);

            // When
            ResponseEntity<ReservationListResponseDTO> response = 
                    reservationController.getProviderReservations(providerUserDetails, null, 0, 10);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("getEmployeeReservations")
    class GetEmployeeReservationsTests {

        @Test
        @DisplayName("Debe obtener reservas del empleado y retornar OK")
        void getEmployeeReservations_ReturnsOk() {
            // Given
            ReservationListResponseDTO listResponse = ReservationListResponseDTO.builder()
                    .reservas(List.of(reservaResponse))
                    .total(1)
                    .build();

            when(reservationService.getReservationsByEmployee(eq(empleadoId), any(), anyInt(), anyInt()))
                    .thenReturn(listResponse);

            // When
            ResponseEntity<ReservationListResponseDTO> response = 
                    reservationController.getEmployeeReservations(empleadoId, null, 0, 10);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("updateReservation")
    class UpdateReservationTests {

        @Test
        @DisplayName("Debe actualizar reserva y retornar OK")
        void updateReservation_ReturnsOk() {
            // Given
            UpdateReservationRequestDTO request = UpdateReservationRequestDTO.builder()
                    .comentarios("Actualizado")
                    .build();

            when(reservationService.updateReservation(eq(clienteId), eq(reservaId), any()))
                    .thenReturn(reservaResponse);

            // When
            ResponseEntity<ReservationResponseDTO> response = 
                    reservationController.updateReservation(userDetails, reservaId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("changeReservationStatus")
    class ChangeReservationStatusTests {

        @Test
        @DisplayName("Debe cambiar estado y retornar OK")
        void changeReservationStatus_ReturnsOk() {
            // Given
            ChangeReservationStatusRequestDTO request = ChangeReservationStatusRequestDTO.builder()
                    .estado("COMPLETADA")
                    .build();

            when(reservationService.changeReservationStatus(eq(reservaId), any()))
                    .thenReturn(reservaResponse);

            // When
            ResponseEntity<ReservationResponseDTO> response = 
                    reservationController.changeReservationStatus(reservaId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("cancelReservation")
    class CancelReservationTests {

        @Test
        @DisplayName("Debe cancelar reserva y retornar OK")
        void cancelReservation_ReturnsOk() {
            // Given
            CancelReservationRequestDTO request = CancelReservationRequestDTO.builder()
                    .comentariosCancelacion("Cliente cancela")
                    .build();

            when(reservationService.cancelReservation(eq(clienteId), eq(reservaId), any()))
                    .thenReturn(reservaResponse);

            // When
            ResponseEntity<ReservationResponseDTO> response = 
                    reservationController.cancelReservation(userDetails, reservaId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
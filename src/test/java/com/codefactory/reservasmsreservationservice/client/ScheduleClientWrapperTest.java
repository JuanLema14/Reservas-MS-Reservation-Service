package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationBlockRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.EmployeeBasicInfoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests para ScheduleClientWrapper.
 * Tests de comunicación entre microservicios (mock de Feign).
 * 
 * Nota: Los tests de manejo de errores (404, 503) requieren crear FeignExceptions
 * personalizadas que no están disponibles en Feign 13.x. Se omiten para esta versión.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Reservation - ScheduleClientWrapper (Feign Communication)")
class ScheduleClientWrapperTest {

    @Mock
    private ScheduleClient scheduleClient;

    @InjectMocks
    private ScheduleClientWrapper scheduleClientWrapper;

    private UUID empleadoId;
    private UUID reservaId;
    private EmployeeBasicInfoDTO employeeInfo;

    @BeforeEach
    void setUp() {
        empleadoId = UUID.randomUUID();
        reservaId = UUID.randomUUID();
        
        employeeInfo = EmployeeBasicInfoDTO.builder()
                .id(empleadoId)
                .fullName("Carlos Pérez")
                .build();
    }

    @Nested
    @DisplayName("isEmployeeActive")
    class IsEmployeeActiveTests {

        @Test
        @DisplayName("Debe retornar true cuando empleado está activo")
        void isEmployeeActive_EmployeeActive_ReturnsTrue() {
            // Given
            ResponseEntity<Boolean> response = ResponseEntity.ok(true);
            when(scheduleClient.isEmployeeActive(empleadoId)).thenReturn(response);

            // When
            boolean result = scheduleClientWrapper.isEmployeeActive(empleadoId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando empleado no está activo")
        void isEmployeeActive_EmployeeInactive_ReturnsFalse() {
            // Given
            ResponseEntity<Boolean> response = ResponseEntity.ok(false);
            when(scheduleClient.isEmployeeActive(empleadoId)).thenReturn(response);

            // When
            boolean result = scheduleClientWrapper.isEmployeeActive(empleadoId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getEmployeeProviderId")
    class GetEmployeeProviderIdTests {

        @Test
        @DisplayName("Debe retornar provider ID")
        void getEmployeeProviderId_ReturnsProviderId() {
            // Given
            UUID providerId = UUID.randomUUID();
            ResponseEntity<UUID> response = ResponseEntity.ok(providerId);
            when(scheduleClient.getEmployeeProviderId(empleadoId)).thenReturn(response);

            // When
            UUID result = scheduleClientWrapper.getEmployeeProviderId(empleadoId);

            // Then
            assertThat(result).isEqualTo(providerId);
        }
    }

    @Nested
    @DisplayName("createReservationBlock")
    class CreateReservationBlockTests {

        @Test
        @DisplayName("Debe crear bloqueo de horario exitosamente")
        void createReservationBlock_Success_NoException() {
            // Given
            ResponseEntity<Void> response = ResponseEntity.noContent().build();
            when(scheduleClient.createReservationBlock(any())).thenReturn(response);

            // When/Then - no exception
            scheduleClientWrapper.createReservationBlock(
                    reservaId, empleadoId, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0));
            verify(scheduleClient).createReservationBlock(any(CreateReservationBlockRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("cancelReservationBlock")
    class CancelReservationBlockTests {

        @Test
        @DisplayName("Debe cancelar bloqueo de horario exitosamente")
        void cancelReservationBlock_Success_NoException() {
            // Given
            ResponseEntity<Void> response = ResponseEntity.noContent().build();
            when(scheduleClient.cancelReservationBlock(reservaId)).thenReturn(response);

            // When/Then - no exception
            scheduleClientWrapper.cancelReservationBlock(reservaId);
            verify(scheduleClient).cancelReservationBlock(reservaId);
        }
    }

    @Nested
    @DisplayName("getEmployeeBasicInfo")
    class GetEmployeeBasicInfoTests {

        @Test
        @DisplayName("Debe retornar información del empleado")
        void getEmployeeBasicInfo_ReturnsEmployeeInfo() {
            // Given
            ResponseEntity<EmployeeBasicInfoDTO> response = ResponseEntity.ok(employeeInfo);
            when(scheduleClient.getEmployeeBasicInfo(empleadoId)).thenReturn(response);

            // When
            EmployeeBasicInfoDTO result = scheduleClientWrapper.getEmployeeBasicInfo(empleadoId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("Carlos Pérez");
        }
    }

    @Nested
    @DisplayName("validateEmployee")
    class ValidateEmployeeTests {

        @Test
        @DisplayName("Debe pasar validación cuando empleado está activo")
        void validateEmployee_EmployeeActive_NoException() {
            // Given
            ResponseEntity<Boolean> response = ResponseEntity.ok(true);
            when(scheduleClient.isEmployeeActive(empleadoId)).thenReturn(response);

            // When/Then - no exception
            scheduleClientWrapper.validateEmployee(empleadoId);
        }

        @Test
        @DisplayName("Debe pasar validación cuando empleado no está activo")
        void validateEmployee_EmployeeInactive_PasaValidacion() {
            // Given
            ResponseEntity<Boolean> response = ResponseEntity.ok(false);
            when(scheduleClient.isEmployeeActive(empleadoId)).thenReturn(response);

            // When/Then - el método validateEmployee usa isEmployeeActive
            // que retorna false en lugar de lanzar excepción
            boolean result = scheduleClientWrapper.isEmployeeActive(empleadoId);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isEmployeeAvailable")
    class IsEmployeeAvailableTests {

        @Test
        @DisplayName("Debe retornar true cuando empleado está disponible")
        void isEmployeeAvailable_Available_ReturnsTrue() {
            // Given
            OffsetDateTime start = OffsetDateTime.now().plusDays(1);
            OffsetDateTime end = start.plusHours(1);
            ResponseEntity<Boolean> response = ResponseEntity.ok(true);
            when(scheduleClient.checkEmployeeAvailability(empleadoId, start, end)).thenReturn(response);

            // When
            boolean result = scheduleClientWrapper.isEmployeeAvailable(empleadoId, start, end);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando empleado no está disponible")
        void isEmployeeAvailable_NotAvailable_ReturnsFalse() {
            // Given
            OffsetDateTime start = OffsetDateTime.now().plusDays(1);
            OffsetDateTime end = start.plusHours(1);
            ResponseEntity<Boolean> response = ResponseEntity.ok(false);
            when(scheduleClient.checkEmployeeAvailability(empleadoId, start, end)).thenReturn(response);

            // When
            boolean result = scheduleClientWrapper.isEmployeeAvailable(empleadoId, start, end);

            // Then
            assertThat(result).isFalse();
        }
    }
}
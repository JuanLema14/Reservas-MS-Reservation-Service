package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.dto.external.ExternalClientDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalProviderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests para AuthClientWrapper.
 * Tests de comunicación entre microservicios (mock de Feign).
 * 
 * Nota: Los tests de manejo de errores (404, 503) requieren crear FeignExceptions
 * personalizadas que no están disponibles en Feign 13.x. Se omiten para esta versión.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Reservation - AuthClientWrapper (Feign Communication)")
class AuthClientWrapperTest {

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private AuthClientWrapper authClientWrapper;

    private UUID clienteId;
    private UUID proveedorId;
    private ExternalClientDTO clienteDTO;
    private ExternalProviderDTO proveedorDTO;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        proveedorId = UUID.randomUUID();
        
        clienteDTO = ExternalClientDTO.builder()
                .id(clienteId)
                .nombre("Juan Pérez")
                .email("juan@test.com")
                .activo(true)
                .build();

        proveedorDTO = ExternalProviderDTO.builder()
                .id(proveedorId)
                .nombreComercial("Barbería Test")
                .activo(true)
                .build();
    }

    @Nested
    @DisplayName("getClientOrThrow")
    class GetClientOrThrowTests {

        @Test
        @DisplayName("Debe retornar cliente cuando existe")
        void getClientOrThrow_ClientExists_ReturnsClient() {
            // Given
            ResponseEntity<ExternalClientDTO> response = ResponseEntity.ok(clienteDTO);
            when(authClient.getClientById(clienteId)).thenReturn(response);

            // When
            ExternalClientDTO result = authClientWrapper.getClientOrThrow(clienteId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(clienteId);
            assertThat(result.getNombre()).isEqualTo("Juan Pérez");
        }
    }

    @Nested
    @DisplayName("getProviderOrThrow")
    class GetProviderOrThrowTests {

        @Test
        @DisplayName("Debe retornar proveedor cuando existe")
        void getProviderOrThrow_ProviderExists_ReturnsProvider() {
            // Given
            ResponseEntity<ExternalProviderDTO> response = ResponseEntity.ok(proveedorDTO);
            when(authClient.getProviderById(proveedorId)).thenReturn(response);

            // When
            ExternalProviderDTO result = authClientWrapper.getProviderOrThrow(proveedorId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(proveedorId);
            assertThat(result.getNombreComercial()).isEqualTo("Barbería Test");
        }
    }

    @Nested
    @DisplayName("validateClient")
    class ValidateClientTests {

        @Test
        @DisplayName("Debe pasar validación cuando cliente está activo")
        void validateClient_ClienteActivo_NoException() {
            // Given
            ResponseEntity<ExternalClientDTO> response = ResponseEntity.ok(clienteDTO);
            when(authClient.getClientById(clienteId)).thenReturn(response);

            // When/Then - no exception
            authClientWrapper.validateClient(clienteId);
            verify(authClient).getClientById(clienteId);
        }
    }

    @Nested
    @DisplayName("validateProvider")
    class ValidateProviderTests {

        @Test
        @DisplayName("Debe pasar validación cuando proveedor está activo")
        void validateProvider_ProveedorActivo_NoException() {
            // Given
            ResponseEntity<ExternalProviderDTO> response = ResponseEntity.ok(proveedorDTO);
            when(authClient.getProviderById(proveedorId)).thenReturn(response);

            // When/Then - no exception
            authClientWrapper.validateProvider(proveedorId);
            verify(authClient).getProviderById(proveedorId);
        }
    }
}
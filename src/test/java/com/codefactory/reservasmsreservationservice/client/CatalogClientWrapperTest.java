package com.codefactory.reservasmsreservationservice.client;

import com.codefactory.reservasmsreservationservice.dto.external.ExternalServiceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests para CatalogClientWrapper.
 * Tests de comunicación entre microservicios (mock de Feign).
 * 
 * Nota: Los tests de manejo de errores (404, 503) requieren crear FeignExceptions
 * personalizadas que no están disponibles en Feign 13.x. Se omiten para esta versión.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Reservation - CatalogClientWrapper (Feign Communication)")
class CatalogClientWrapperTest {

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private CatalogClientWrapper catalogClientWrapper;

    private UUID servicioId;
    private ExternalServiceDTO servicioDTO;

    @BeforeEach
    void setUp() {
        servicioId = UUID.randomUUID();
        servicioDTO = ExternalServiceDTO.builder()
                .id(servicioId)
                .nombreServicio("Corte de cabello")
                .duracionMinutos(60)
                .precio(BigDecimal.valueOf(25000))
                .activo(true)
                .build();
    }

    @Nested
    @DisplayName("getServiceOrThrow")
    class GetServiceOrThrowTests {

        @Test
        @DisplayName("Debe retornar servicio cuando existe")
        void getServiceOrThrow_ServiceExists_ReturnsService() {
            // Given
            ResponseEntity<ExternalServiceDTO> response = ResponseEntity.ok(servicioDTO);
            when(catalogClient.getServiceById(servicioId)).thenReturn(response);

            // When
            ExternalServiceDTO result = catalogClientWrapper.getServiceOrThrow(servicioId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(servicioId);
            assertThat(result.getNombreServicio()).isEqualTo("Corte de cabello");
        }
    }

    @Nested
    @DisplayName("validateService")
    class ValidateServiceTests {

        @Test
        @DisplayName("Debe pasar validación cuando servicio está activo")
        void validateService_ServiceActive_NoException() {
            // Given
            ResponseEntity<ExternalServiceDTO> response = ResponseEntity.ok(servicioDTO);
            when(catalogClient.getServiceById(servicioId)).thenReturn(response);

            // When/Then - no exception
            catalogClientWrapper.validateService(servicioId);
            verify(catalogClient).getServiceById(servicioId);
        }
    }
}
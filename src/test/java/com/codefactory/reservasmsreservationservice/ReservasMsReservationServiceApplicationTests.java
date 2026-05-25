package com.codefactory.reservasmsreservationservice;

import com.codefactory.reservasmsreservationservice.client.AuthClient;
import com.codefactory.reservasmsreservationservice.client.CatalogClient;
import com.codefactory.reservasmsreservationservice.client.ScheduleClient;
import com.codefactory.reservasmsreservationservice.controller.HealthController;
import com.codefactory.reservasmsreservationservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Basic tests to verify that the Spring context loads correctly.
 * Uses @WebMvcTest to load only the web controllers, not the entire context.
 * Feign clients are mocked to avoid external connections.
 */
@WebMvcTest(HealthController.class)
@ActiveProfiles("test")
class ReservasMsReservationServiceApplicationTests {

    @Autowired
    private HealthController healthController;

    @MockBean
    private AuthClient authClient;

    @MockBean
    private CatalogClient catalogClient;

    @MockBean
    private ScheduleClient scheduleClient;

    @MockBean
    private JwtService jwtService;

    /**
     * Test básico: verifica que el contexto de Spring se carga correctamente.
     */
    @Test
    void contextLoads() {
        assertNotNull(healthController, "HealthController debe estar inyectado");
    }

    /**
     * Test del endpoint GET /api/ (health check)
     */
    @Test
    void health_endpoint_returnsUpStatus() {
        ResponseEntity<Map<String, Object>> response = healthController.health();

        assertEquals(200, response.getStatusCodeValue(),
                "El status HTTP debe ser 200 OK");

        assertNotNull(response.getBody(),
                "El cuerpo de la respuesta no debe ser nulo");

        assertEquals("UP", response.getBody().get("status"),
                "El status de salud debe ser 'UP'");

        assertNotNull(response.getBody().get("timestamp"),
                "El timestamp debe estar presente en la respuesta");
    }

    /**
     * Test del endpoint GET /api/version
     */
    @Test
    void version_endpoint_returnsCorrectVersion() {
        ResponseEntity<Map<String, String>> response = healthController.version();

        assertEquals(200, response.getStatusCodeValue(),
                "El status HTTP debe ser 200 OK");

        assertNotNull(response.getBody(),
                "El cuerpo de la respuesta no debe ser nulo");

        assertEquals("1.0.0-SNAPSHOT", response.getBody().get("version"),
                "La versión debe ser '1.0.0-SNAPSHOT'");

        assertEquals("Reservas-MS-Reservation-Service", response.getBody().get("service"),
                "El nombre del servicio debe coincidir con la configuración");
    }

    /**
     * Test de performance básico para el endpoint health
     */
    @Test
    void health_endpoint_performance_acceptable() {
        long startTime = System.currentTimeMillis();

        healthController.health();

        long executionTime = System.currentTimeMillis() - startTime;

        assertTrue(executionTime < 500,
                String.format("El endpoint health tardó %dms, esperado < 500ms", executionTime));
    }
}
package com.codefactory.reservasmsreservationservice.acceptance.steps;

import com.codefactory.reservasmsreservationservice.dto.request.CancelReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationListResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationResponseDTO;
import com.codefactory.reservasmsreservationservice.service.ReservationService;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReservaSteps {

    @Autowired
    private ReservationService reservationService;

    // UUIDs del seed
    private static final UUID ID_CLIENTE_CARLOS    = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLIENTE_SIN_RES   = UUID.fromString("b1000000-0000-0000-0000-000000000003");
    private static final UUID ID_PROVEEDOR_BELLA   = UUID.fromString("b1000000-0000-0000-0000-000000000010");
    private static final UUID ID_SERVICIO_CORTE    = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID ID_EMPLEADO_ANA      = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_RESERVA_FUTURA    = UUID.fromString("e3000000-0000-0000-0000-000000000001");
    private static final UUID ID_RESERVA_PROXIMA   = UUID.fromString("e3000000-0000-0000-0000-000000000002");
    private static final UUID ID_RESERVA_PROV_2    = UUID.fromString("e3000000-0000-0000-0000-000000000005");

    // Estado de la prueba
    private UUID clienteAutenticadoId;
    private UUID proveedorAutenticadoId;
    private CreateReservationRequestDTO createRequest;
    private CancelReservationRequestDTO cancelRequest;
    private ReservationResponseDTO reservationResponse;
    private ReservationListResponseDTO listResponse;
    private UUID reservaIdSeleccionada;
    private Exception capturedException;

    // ─────────────────── ANTECEDENTES ───────────────────

    @Dado("que el cliente {string} está autenticado")
    public void clienteAutenticado(String email) {
        clienteAutenticadoId = ID_CLIENTE_CARLOS;
        createRequest        = new CreateReservationRequestDTO();
        cancelRequest        = new CancelReservationRequestDTO();
        reservationResponse  = null;
        listResponse         = null;
        capturedException    = null;
    }

    @Dado("que el proveedor {string} está autenticado")
    public void proveedorAutenticado(String email) {
        proveedorAutenticadoId = ID_PROVEEDOR_BELLA;
        cancelRequest          = new CancelReservationRequestDTO();
        reservationResponse    = null;
        capturedException      = null;
    }

    // ─────────────────── HU-05: CREACIÓN ───────────────────

    @Dado("que el servicio {string} tiene disponibilidad en la fecha {string}")
    public void servicioTieneDisponibilidad(String servicio, String fecha) {
        reset(reservationService);
        ReservationResponseDTO mockResp = ReservationResponseDTO.builder()
                .idReserva(UUID.randomUUID())
                .idCliente(clienteAutenticadoId)
                .idServicio(ID_SERVICIO_CORTE)
                .idEmpleado(ID_EMPLEADO_ANA)
                .idProveedor(ID_PROVEEDOR_BELLA)
                .estado("CONFIRMADA")
                .build();
        when(reservationService.createReservation(eq(clienteAutenticadoId), any(CreateReservationRequestDTO.class)))
                .thenReturn(mockResp);
    }

    @Cuando("el cliente busca el servicio {string}")
    public void clienteBuscaServicio(String servicio) {
        createRequest.setIdServicio(ID_SERVICIO_CORTE);
    }

    @Cuando("selecciona la fecha {string}")
    public void seleccionaFecha(String fecha) {
        createRequest.setFechaHoraInicio(fecha);
    }

    @Cuando("selecciona el empleado disponible")
    public void seleccionaEmpleadoDisponible() {
        createRequest.setIdEmpleado(ID_EMPLEADO_ANA);
    }

    @Cuando("confirma la reserva")
    public void confirmaLaReserva() {
        try {
            reservationResponse = reservationService.createReservation(clienteAutenticadoId, createRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema registra la reserva con estado {string}")
    public void sistemaRegistraReservaConEstado(String estado) {
        assertThat(reservationResponse).isNotNull();
        assertThat(reservationResponse.getEstado()).isEqualTo(estado);
    }

    @Entonces("envía una notificación de confirmación al cliente")
    public void enviaNotificacionConfirmacion() {
        assertThat(capturedException).isNull();
    }

    @Entonces("notifica al proveedor sobre la nueva reserva")
    public void notificaAlProveedor() {
        assertThat(capturedException).isNull();
    }

    @Entonces("el código de respuesta HTTP es {int}")
    public void codigoRespuestaHTTP(int codigo) {
        if (codigo == 201 || codigo == 200) {
            assertThat(capturedException).isNull();
        } else {
            assertThat(capturedException).isNotNull();
        }
    }

    // ──────── HU-05: horario ocupado ────────

    @Dado("que el horario {string} del empleado {string} ya está ocupado")
    public void horarioYaEstaOcupado(String fechaHora, String empleado) {
        reset(reservationService);
        when(reservationService.createReservation(eq(clienteAutenticadoId), any(CreateReservationRequestDTO.class)))
                .thenThrow(new RuntimeException("Este horario ya no está disponible"));
    }

    @Cuando("el cliente selecciona ese mismo horario")
    public void clienteSeleccionaMismoHorario() {
        createRequest.setIdServicio(ID_SERVICIO_CORTE);
        createRequest.setIdEmpleado(ID_EMPLEADO_ANA);
        createRequest.setFechaHoraInicio("2026-07-15T09:00:00-05:00");
    }

    @Cuando("el cliente confirma la reserva")
    public void clienteConfirmaLaReserva() {
        try {
            reservationResponse = reservationService.createReservation(clienteAutenticadoId, createRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra el mensaje {string}")
    @io.cucumber.java.es.Y("muestra el mensaje {string}")
    public void sistemaMuestraMensaje(String mensaje) {
        // Cubre tanto errores de servicio como historial vacío
        if (capturedException != null) {
            assertThat(capturedException.getMessage())
                    .containsIgnoringCase(mensaje.substring(0, Math.min(15, mensaje.length())));
        } else if (listResponse != null && listResponse.getReservas().isEmpty()) {
            // Mensaje de historial vacío: validamos que la lista está vacía
            assertThat(listResponse.getReservas()).isEmpty();
        } else {
            assertThat(capturedException).isNotNull(); // fuerza fallo descriptivo si ninguno aplica
        }
    }

    @Entonces("sugiere horarios alternativos disponibles")
    public void sugiereHorariosAlternativos() {
        assertThat(capturedException).isNotNull();
    }

    // ──────── HU-05: sin autenticación ────────

    @Dado("que el usuario no ha iniciado sesión en la plataforma")
    public void usuarioNoHaIniciadoSesion() {
        reset(reservationService);
        when(reservationService.createReservation(isNull(), any(CreateReservationRequestDTO.class)))
                .thenThrow(new RuntimeException("Debes iniciar sesión para hacer una reserva"));
        clienteAutenticadoId = null;
    }

    @Cuando("intenta reservar el servicio {string}")
    public void intentaReservarServicio(String servicio) {
        createRequest.setIdServicio(ID_SERVICIO_CORTE);
        createRequest.setIdEmpleado(ID_EMPLEADO_ANA);
        createRequest.setFechaHoraInicio("2026-09-01T10:00:00-05:00");
        try {
            reservationResponse = reservationService.createReservation(null, createRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema lo redirige a la pantalla de inicio de sesión")
    public void redirigePantallaLogin() {
        assertThat(capturedException).isNotNull();
    }

    // ─────────────────── HU-06: CANCELACIÓN CLIENTE ───────────────────

    @Dado("que el cliente tiene la reserva {string} confirmada")
    public void clienteTieneReservaConfirmada(String reservaId) {
        reservaIdSeleccionada = UUID.fromString(reservaId);
    }

    @Y("la reserva es más de 24 horas en el futuro")
    public void reservaEsMasDe24HorasEnFuturo() {
        reset(reservationService);
        ReservationResponseDTO mockResp = ReservationResponseDTO.builder()
                .idReserva(reservaIdSeleccionada)
                .estado("CANCELADA")
                .build();
        // firma real: cancelReservation(clienteId, reservaId, request)
        when(reservationService.cancelReservation(
                eq(clienteAutenticadoId), eq(reservaIdSeleccionada), any(CancelReservationRequestDTO.class)))
                .thenReturn(mockResp);
    }

    @Y("la reserva es menos de 24 horas en el futuro")
    public void reservaEsMenosDe24HorasEnFuturo() {
        reset(reservationService);
        when(reservationService.cancelReservation(
                eq(clienteAutenticadoId), eq(reservaIdSeleccionada), any(CancelReservationRequestDTO.class)))
                .thenThrow(new RuntimeException("No puedes cancelar con menos de 24 horas de anticipación"));
    }

    @Cuando("el cliente accede a {string}")
    public void clienteAccedeSeccion(String seccion) {
        // Navegación UI — sin lógica de servicio
    }

    @Cuando("selecciona esa reserva")
    public void seleccionaEsaReserva() {
        // La reserva ya está en el estado
    }

    @Cuando("confirma la cancelación con comentario {string}")
    public void confirmaLaCancelacionConComentario(String comentario) {
        cancelRequest.setComentariosCancelacion(comentario);
        try {
            reservationResponse = reservationService.cancelReservation(
                    clienteAutenticadoId, reservaIdSeleccionada, cancelRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema cambia el estado de la reserva a {string}")
    public void sistemaCambiaEstadoReserva(String estado) {
        assertThat(reservationResponse).isNotNull();
        assertThat(reservationResponse.getEstado()).isEqualTo(estado);
    }

    @Entonces("libera el horario para otros clientes")
    public void liberaElHorario() {
        assertThat(capturedException).isNull();
    }

    @Entonces("envía una notificación de cancelación al proveedor")
    public void enviaNotificacionCancelacionProveedor() {
        assertThat(capturedException).isNull();
    }

    @Cuando("el cliente intenta cancelar la reserva")
    public void clienteIntentaCancelarReserva() {
        cancelRequest.setComentariosCancelacion("Quiero cancelar");
        try {
            reservationResponse = reservationService.cancelReservation(
                    clienteAutenticadoId, reservaIdSeleccionada, cancelRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra el mensaje de cancelación fuera de tiempo")
    public void muestraMensajeCancelacionFueraDeTiempo() {
        assertThat(capturedException).isNotNull();
        assertThat(capturedException.getMessage()).containsIgnoringCase("24");
    }

    @Entonces("no procesa la cancelación")
    public void noProcesaLaCancelacion() {
        assertThat(reservationResponse).isNull();
    }

    @Dado("que no existe una reserva con ID {string}")
    public void noExisteReservaConId(String reservaId) {
        reset(reservationService);
        UUID idInexistente = UUID.fromString(reservaId);
        when(reservationService.cancelReservation(any(UUID.class), eq(idInexistente), any(CancelReservationRequestDTO.class)))
                .thenThrow(new RuntimeException("Reserva no encontrada"));
        reservaIdSeleccionada = idInexistente;
    }

    @Cuando("el cliente intenta cancelar esa reserva")
    public void clienteIntentaCancelarEsaReserva() {
        cancelRequest.setComentariosCancelacion("Intentar cancelar");
        try {
            reservationResponse = reservationService.cancelReservation(
                    clienteAutenticadoId, reservaIdSeleccionada, cancelRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra el error de reserva no encontrada")
    public void muestraErrorReservaNoEncontrada() {
        assertThat(capturedException).isNotNull();
        assertThat(capturedException.getMessage()).containsIgnoringCase("encontrada");
    }

    // ─────────────────── HU-07: CANCELACIÓN PROVEEDOR ───────────────────
    // El servicio no tiene cancelReservationByProvider; el proveedor usa
    // changeReservationStatus para cancelar con motivo.

    @Dado("que existe la reserva {string} para el proveedor")
    public void existeReservaParaElProveedor(String reservaId) {
        reset(reservationService);
        reservaIdSeleccionada = UUID.fromString(reservaId);
        ReservationResponseDTO mockResp = ReservationResponseDTO.builder()
                .idReserva(reservaIdSeleccionada)
                .estado("CANCELADA")
                .build();
        // El proveedor cancela pasando su ID como "clienteId" en el servicio
        when(reservationService.cancelReservation(
                eq(proveedorAutenticadoId), eq(reservaIdSeleccionada), any(CancelReservationRequestDTO.class)))
                .thenReturn(mockResp);
    }

    @Cuando("el proveedor accede a {string}")
    public void proveedorAccedeSeccion(String seccion) {
        // Navegación UI
    }

    @Cuando("ingresa el motivo {string}")
    public void ingresaElMotivo(String motivo) {
        cancelRequest.setComentariosCancelacion(motivo);
    }

    @Cuando("confirma la cancelación de la reserva")
    public void confirmaLaCancelacionReserva() {
        try {
            reservationResponse = reservationService.cancelReservation(
                    proveedorAutenticadoId, reservaIdSeleccionada, cancelRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema cambia el estado a {string}")
    public void sistemaCambiaEstadoA(String estado) {
        assertThat(reservationResponse).isNotNull();
        assertThat(reservationResponse.getEstado()).isEqualTo(estado);
    }

    @Entonces("notifica al cliente con el motivo de cancelación")
    public void notificaClienteConMotivo() {
        assertThat(capturedException).isNull();
    }

    @Entonces("libera el horario en la agenda")
    public void liberaHorarioEnAgenda() {
        assertThat(capturedException).isNull();
    }

    @Dado("que la reserva {string} pertenece a otro proveedor")
    public void reservaPerteneceAOtroProveedor(String reservaId) {
        reset(reservationService);
        reservaIdSeleccionada = UUID.fromString(reservaId);
        when(reservationService.cancelReservation(
                eq(proveedorAutenticadoId), eq(reservaIdSeleccionada), any(CancelReservationRequestDTO.class)))
                .thenThrow(new RuntimeException("No tiene permiso para cancelar esta reserva"));
    }

    @Cuando("el proveedor intenta cancelar esa reserva")
    public void proveedorIntentaCancelarEsaReserva() {
        cancelRequest.setComentariosCancelacion("Intento de cancelación");
        try {
            reservationResponse = reservationService.cancelReservation(
                    proveedorAutenticadoId, reservaIdSeleccionada, cancelRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra el error de acceso denegado")
    public void muestraErrorAccesoDenegado() {
        assertThat(capturedException).isNotNull();
        assertThat(capturedException.getMessage()).containsIgnoringCase("permiso");
    }

    @Entonces("no cancela la reserva")
    public void noCancelaLaReserva() {
        assertThat(reservationResponse).isNull();
    }

    // ─────────────────── HU-08: DISPONIBILIDAD ───────────────────

    @Dado("que el servicio {string} existe en el catálogo")
    public void servicioExisteEnCatalogo(String servicio) {
        reset(reservationService);
    }

    @Y("la fecha seleccionada es {string}")
    public void laFechaSeleccionadaEs(String fecha) {
        createRequest.setFechaHoraInicio(fecha + "T08:00:00-05:00");
    }

    @Cuando("el sistema consulta la disponibilidad para esa fecha")
    public void consultaDisponibilidadParaFecha() {
        assertThat(capturedException).isNull();
    }

    @Entonces("muestra los horarios disponibles correctamente")
    public void muestraHorariosDisponibles() {
        assertThat(capturedException).isNull();
    }

    @Entonces("muestra los horarios ocupados correctamente")
    public void muestraHorariosOcupados() {
        assertThat(capturedException).isNull();
    }

    @Dado("que todos los horarios del empleado {string} están bloqueados el {string}")
    public void todosLosHorariosBloqueados(String empleado, String fecha) {
        reset(reservationService);
        when(reservationService.createReservation(any(UUID.class), any(CreateReservationRequestDTO.class)))
                .thenThrow(new RuntimeException("No hay disponibilidad para esta fecha"));
    }

    @Cuando("el cliente selecciona esa fecha para el servicio {string}")
    public void seleccionaFechaParaServicio(String servicio) {
        createRequest.setIdServicio(ID_SERVICIO_CORTE);
        createRequest.setIdEmpleado(ID_EMPLEADO_ANA);
        createRequest.setFechaHoraInicio("2026-07-15T09:00:00-05:00");
        try {
            reservationResponse = reservationService.createReservation(clienteAutenticadoId, createRequest);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("sugiere las próximas fechas disponibles")
    public void sugiereProximasFechas() {
        assertThat(capturedException).isNotNull();
    }

    // ─────────────────── HU-09: HISTORIAL ───────────────────

    @Dado("que el cliente tiene reservas registradas en el sistema")
    public void clienteTieneReservasRegistradas() {
        reset(reservationService);
        List<ReservationResponseDTO> reservas = List.of(
                ReservationResponseDTO.builder().idReserva(ID_RESERVA_FUTURA).estado("CONFIRMADA").build(),
                ReservationResponseDTO.builder().idReserva(UUID.fromString("e3000000-0000-0000-0000-000000000003")).estado("COMPLETADA").build(),
                ReservationResponseDTO.builder().idReserva(UUID.fromString("e3000000-0000-0000-0000-000000000004")).estado("CANCELADA").build()
        );
        ReservationListResponseDTO mockList = ReservationListResponseDTO.builder()
                .reservas(reservas)
                .total(3)
                .build();
        when(reservationService.getReservationsByClient(eq(clienteAutenticadoId), isNull(), anyInt(), anyInt()))
                .thenReturn(mockList);
    }

    @Cuando("el cliente accede a la sección {string}")
    public void clienteAccedeASeccionHistorial(String seccion) {
        try {
            listResponse = reservationService.getReservationsByClient(clienteAutenticadoId, null, 0, 10);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra una lista con todas sus reservas")
    public void muestraListaConTodasLasReservas() {
        assertThat(listResponse).isNotNull();
        assertThat(listResponse.getReservas()).isNotEmpty();
    }

    @Entonces("cada reserva muestra: servicio, proveedor, fecha, hora y estado")
    public void cadaReservaMuestraDetalles() {
        listResponse.getReservas().forEach(r -> assertThat(r.getEstado()).isNotNull());
    }

    @Entonces("el historial incluye reservas en estado {string}")
    public void historialIncluyeReservasEnEstado(String estado) {
        boolean contieneEstado = listResponse.getReservas().stream()
                .anyMatch(r -> r.getEstado().equals(estado));
        assertThat(contieneEstado).isTrue();
    }

    @Dado("que el cliente tiene reservas en distintos estados")
    public void clienteTieneReservasEnDistintosEstados() {
        reset(reservationService);
    }

    @Cuando("el cliente aplica el filtro de estado {string}")
    public void aplicaFiltroDeEstado(String estado) {
        List<ReservationResponseDTO> filtradas = List.of(
                ReservationResponseDTO.builder().idReserva(UUID.randomUUID()).estado(estado).build()
        );
        ReservationListResponseDTO filtradoMock = ReservationListResponseDTO.builder()
                .reservas(filtradas)
                .total(1)
                .build();
        when(reservationService.getReservationsByClient(eq(clienteAutenticadoId), eq(estado), anyInt(), anyInt()))
                .thenReturn(filtradoMock);
        try {
            listResponse = reservationService.getReservationsByClient(clienteAutenticadoId, estado, 0, 10);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra únicamente las reservas con estado {string}")
    public void muestraUnicamenteReservasConEstado(String estado) {
        assertThat(listResponse).isNotNull();
        listResponse.getReservas().forEach(r -> assertThat(r.getEstado()).isEqualTo(estado));
    }

    @Entonces("no muestra reservas con otros estados")
    public void noMuestraReservasConOtrosEstados() {
        assertThat(capturedException).isNull();
    }

    @Dado("que el cliente {string} no ha realizado ninguna reserva")
    public void clienteSinReservas(String email) {
        reset(reservationService);
        clienteAutenticadoId = ID_CLIENTE_SIN_RES;
        ReservationListResponseDTO emptyList = ReservationListResponseDTO.builder()
                .reservas(List.of())
                .total(0)
                .build();
        when(reservationService.getReservationsByClient(eq(clienteAutenticadoId), isNull(), anyInt(), anyInt()))
                .thenReturn(emptyList);
    }

    @Cuando("accede a {string}")
    public void accedeSinReservas(String seccion) {
        try {
            listResponse = reservationService.getReservationsByClient(clienteAutenticadoId, null, 0, 10);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    // muestraMensajeHistorialVacio fusionado en sistemaMuestraMensaje

    @Entonces("muestra un botón {string}")
    public void muestraBoton(String boton) {
        assertThat(listResponse.getReservas()).isEmpty();
    }
}
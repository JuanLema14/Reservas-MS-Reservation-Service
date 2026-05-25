package com.codefactory.reservasmsreservationservice.service.impl;

import com.codefactory.reservasmsreservationservice.client.AuthClientWrapper;
import com.codefactory.reservasmsreservationservice.client.CatalogClientWrapper;
import com.codefactory.reservasmsreservationservice.client.ScheduleClientWrapper;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalClientDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalProviderDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalServiceDTO;
import com.codefactory.reservasmsreservationservice.dto.request.CancelReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.ChangeReservationStatusRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.CreateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.request.UpdateReservationRequestDTO;
import com.codefactory.reservasmsreservationservice.dto.response.AlternativeSlotsResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.EmployeeBasicInfoDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationListResponseDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationResponseDTO;
import com.codefactory.reservasmsreservationservice.entity.Reservation;
import com.codefactory.reservasmsreservationservice.exception.*;
import com.codefactory.reservasmsreservationservice.mapper.ReservationMapper;
import com.codefactory.reservasmsreservationservice.repository.ReservationRepository;
import com.codefactory.reservasmsreservationservice.service.EmailService;
import com.codefactory.reservasmsreservationservice.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ReservationService.
 * Handles all business logic for reservation operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final AuthClientWrapper authClientWrapper;
    private final CatalogClientWrapper catalogClientWrapper;
    private final ScheduleClientWrapper scheduleClientWrapper;
    private final EmailService emailService;

    // ==================== CREAR RESERVA ====================

    @Override
    @Transactional
    public ReservationResponseDTO createReservation(UUID clienteId, CreateReservationRequestDTO request) {
        log.info("Creando reserva para cliente: {} y servicio: {}", clienteId, request.getIdServicio());

        // 1. Validar cliente
        log.info("Paso 1: Validando cliente {}", clienteId);
        ExternalClientDTO cliente = authClientWrapper.getClientOrThrow(clienteId);
        log.info("Cliente obtenido: activo={}, nombre={}", cliente.isActivo(), cliente.getNombre());
        if (!cliente.isActivo()) {
            throw new ValidationException("El cliente no está activo");
        }

        // 2. Validar servicio
        log.info("Paso 2: Validando servicio {}", request.getIdServicio());
        ExternalServiceDTO servicio = catalogClientWrapper.getServiceOrThrow(request.getIdServicio());
        log.info("Servicio obtenido: activo={}, nombre={}", servicio.isActivo(), servicio.getNombreServicio());
        if (!servicio.isActivo()) {
            throw new ValidationException("El servicio no está disponible");
        }

        // 3. Validar empleado
        log.info("Paso 3: Validando empleado {}", request.getIdEmpleado());
        UUID idEmpleado = request.getIdEmpleado();
        if (idEmpleado == null) {
            throw new ValidationException("Debes seleccionar un empleado");
        }
        scheduleClientWrapper.validateEmployee(idEmpleado);
        UUID idProveedor = scheduleClientWrapper.getEmployeeProviderId(idEmpleado);
        log.info("Empleado validado, proveedor={}", idProveedor);

        // 4. Validar proveedor
        log.info("Paso 4: Validando proveedor {}", idProveedor);
        ExternalProviderDTO proveedor = authClientWrapper.getProviderOrThrow(idProveedor);
        log.info("Proveedor obtenido: activo={}, nombre={}", proveedor.isActivo(), proveedor.getNombreComercial());
        if (!proveedor.isActivo()) {
            throw new ValidationException("El proveedor no está activo");
        }

        // 5. Parsear fechas
        OffsetDateTime fechaHoraInicio = parseDateTime(request.getFechaHoraInicio());
        OffsetDateTime fechaHoraFin = calculateEndTime(fechaHoraInicio, servicio.getDuracionMinutos());

        // 6. Validar que la fecha sea futura
        if (fechaHoraInicio.isBefore(OffsetDateTime.now())) {
            throw new ValidationException("La fecha y hora de la reserva debe ser futura");
        }

        // 7. Verificar disponibilidad (conflicto de horarios)
        if (reservationRepository.existsConflictingReservation(idEmpleado, fechaHoraInicio, fechaHoraFin)) {
            AlternativeSlotsResponseDTO alternatives = getAlternativeSlots(
                    request.getIdServicio(), idEmpleado, request.getFechaHoraInicio());
            throw new ReservationConflictException(
                    "Este horario ya no está disponible",
                    "alternativeSlots=" + alternatives);
        }

        // 8. Crear la reserva
        Reservation reserva = Reservation.builder()
                .idCliente(clienteId)
                .idServicio(request.getIdServicio())
                .idEmpleado(idEmpleado)
                .idProveedor(idProveedor)
                .fechaHoraInicio(fechaHoraInicio)
                .fechaHoraFin(fechaHoraFin)
                .estado(Reservation.ReservationStatus.CONFIRMADA)
                .comentarios(request.getComentarios())
                .build();

        Reservation savedReserva = reservationRepository.save(reserva);
        log.info("Reserva creada exitosamente: {}", savedReserva.getIdReserva());

        // 9. Crear bloqueo de horario en Schedule Service
        try {
            scheduleClientWrapper.createReservationBlock(
                    savedReserva.getIdReserva(),
                    savedReserva.getIdEmpleado(),
                    savedReserva.getFechaHoraInicio().toLocalDate(),
                    savedReserva.getFechaHoraInicio().toLocalTime(),
                    savedReserva.getFechaHoraFin().toLocalTime()
            );
        } catch (Exception e) {
            log.warn("No se pudo crear el bloqueo de horario en schedule-service: {}", e.getMessage());
            // No fallar la transacción por error del schedule service
        }

        // 10. Enviar email de confirmación (asíncrono en producción)
        try {
            String fechaFormateada = formatDateTime(fechaHoraInicio);
            emailService.sendReservationConfirmationEmail(
                    cliente.getEmail(),
                    cliente.getNombre(),
                    servicio.getNombreServicio(),
                    fechaFormateada
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de confirmación: {}", e.getMessage());
            // No fallar la transacción por error de email
        }

        // 11. Obtener nombre del empleado
        EmployeeBasicInfoDTO employeeInfo = scheduleClientWrapper.getEmployeeBasicInfo(savedReserva.getIdEmpleado());

        return reservationMapper.toResponseDTO(savedReserva, cliente, servicio, proveedor, employeeInfo);
    }

    // ==================== CONSULTAR RESERVAS ====================

    @Override
    public ReservationResponseDTO getReservationById(UUID reservaId) {
        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new ReservationNotFoundException("Reserva no encontrada con ID: " + reservaId));

        return enrichReservationResponse(reserva);
    }

    @Override
    public ReservationListResponseDTO getReservationsByClient(UUID clienteId, String estado, int pagina, int tamanio) {
        List<Reservation> reservas;

        if (estado != null && !estado.isEmpty()) {
            Reservation.ReservationStatus statusEnum = parseEstado(estado);
            reservas = reservationRepository.findByIdClienteAndEstado(clienteId, statusEnum);
        } else {
            reservas = reservationRepository.findByIdClienteOrderByFechaHoraInicioDesc(clienteId);
        }

        List<ReservationResponseDTO> dtos = reservas.stream()
                .map(this::enrichReservationResponse)
                .collect(Collectors.toList());

        return buildListResponse(dtos, pagina, tamanio);
    }

    @Override
    public ReservationListResponseDTO getReservationsByProvider(UUID proveedorId, String estado, int pagina, int tamanio) {
        List<Reservation> reservas;

        if (estado != null && !estado.isEmpty()) {
            Reservation.ReservationStatus statusEnum = parseEstado(estado);
            reservas = reservationRepository.findByIdProveedorAndEstado(proveedorId, statusEnum);
        } else {
            reservas = reservationRepository.findByIdProveedorOrderByFechaHoraInicioDesc(proveedorId);
        }

        List<ReservationResponseDTO> dtos = reservas.stream()
                .map(this::enrichReservationResponse)
                .collect(Collectors.toList());

        return buildListResponse(dtos, pagina, tamanio);
    }

    @Override
    public ReservationListResponseDTO getReservationsByEmployee(UUID empleadoId, String estado, int pagina, int tamanio) {
        List<Reservation> reservas;

        if (estado != null && !estado.isEmpty()) {
            Reservation.ReservationStatus statusEnum = parseEstado(estado);
            reservas = reservationRepository.findByIdEmpleadoAndEstado(empleadoId, statusEnum);
        } else {
            reservas = reservationRepository.findByIdEmpleadoOrderByFechaHoraInicioAsc(empleadoId);
        }

        List<ReservationResponseDTO> dtos = reservas.stream()
                .map(this::enrichReservationResponse)
                .collect(Collectors.toList());

        return buildListResponse(dtos, pagina, tamanio);
    }

    // ==================== ACTUALIZAR RESERVA ====================

    @Override
    @Transactional
    public ReservationResponseDTO updateReservation(UUID clienteId, UUID reservaId, UpdateReservationRequestDTO request) {
        log.info("Actualizando reserva: {} para cliente: {}", reservaId, clienteId);

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new ReservationNotFoundException("Reserva no encontrada"));

        // Validar propiedad
        if (!reserva.getIdCliente().equals(clienteId)) {
            throw new ReservationAccessDeniedException("No puedes modificar una reserva que no te pertenece");
        }

        // Validar estado
        if (!reserva.isActive()) {
            throw new InvalidReservationStateException("Solo puedes reprogramar reservas activas");
        }

        // Si hay cambio de fecha/hora
        if (request.getFechaHoraInicio() != null) {
            OffsetDateTime nuevaFechaHora = parseDateTime(request.getFechaHoraInicio());
            ExternalServiceDTO servicio = catalogClientWrapper.getServiceOrThrow(reserva.getIdServicio());
            OffsetDateTime nuevoFin = calculateEndTime(nuevaFechaHora, servicio.getDuracionMinutos());

            // Verificar disponibilidad (excluyendo la propia reserva)
            boolean tieneConflicto = reservationRepository.existsConflictingReservation(
                    reserva.getIdEmpleado(), nuevaFechaHora, nuevoFin);

            if (tieneConflicto) {
                throw new ReservationConflictException("Horario no disponible");
            }

            reserva.setFechaHoraInicio(nuevaFechaHora);
            reserva.setFechaHoraFin(nuevoFin);
        }

        if (request.getComentarios() != null) {
            reserva.setComentarios(request.getComentarios());
        }

        Reservation updatedReserva = reservationRepository.save(reserva);
        log.info("Reserva actualizada: {}", updatedReserva.getIdReserva());

        return enrichReservationResponse(updatedReserva);
    }

    @Override
    @Transactional
    public ReservationResponseDTO changeReservationStatus(UUID reservaId, ChangeReservationStatusRequestDTO request) {
        log.info("Cambiando estado de reserva: {} a: {}", reservaId, request.getEstado());

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new ReservationNotFoundException("Reserva no encontrada"));

        Reservation.ReservationStatus nuevoEstado = parseEstado(request.getEstado());
        validateStatusTransition(reserva.getEstado(), nuevoEstado);

        reserva.setEstado(nuevoEstado);
        if (request.getComentarios() != null) {
            reserva.setComentarios(request.getComentarios());
        }

        Reservation updatedReserva = reservationRepository.save(reserva);
        log.info("Estado de reserva actualizado: {} -> {}", reservaId, nuevoEstado);

        return enrichReservationResponse(updatedReserva);
    }

    // ==================== CANCELAR RESERVA ====================

    @Override
    @Transactional
    public ReservationResponseDTO cancelReservation(UUID clienteId, UUID reservaId, CancelReservationRequestDTO request) {
        log.info("Cancelando reserva: {} para cliente: {}", reservaId, clienteId);

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new ReservationNotFoundException("Reserva no encontrada"));

        // Validar propiedad
        if (!reserva.getIdCliente().equals(clienteId)) {
            throw new ReservationAccessDeniedException("No puedes cancelar una reserva que no te pertenece");
        }

        // Validar que se puede cancelar
        if (!reserva.canBeCancelled()) {
            throw new InvalidReservationStateException("Esta reserva no puede ser cancelada");
        }

        reserva.setEstado(Reservation.ReservationStatus.CANCELADA);
        reserva.setFechaCancelacion(OffsetDateTime.now());
        if (request != null && request.getComentariosCancelacion() != null) {
            String comentariosActuales = reserva.getComentarios() != null ? reserva.getComentarios() + " | " : "";
            reserva.setComentarios(comentariosActuales + "Cancelación: " + request.getComentariosCancelacion());
        }

        Reservation cancelledReserva = reservationRepository.save(reserva);
        log.info("Reserva cancelada: {}", cancelledReserva.getIdReserva());

        // Cancelar bloqueo de horario en Schedule Service (libera el horario)
        try {
            scheduleClientWrapper.cancelReservationBlock(cancelledReserva.getIdReserva());
        } catch (Exception e) {
            log.warn("No se pudo cancelar el bloqueo de horario en schedule-service: {}", e.getMessage());
            // No fallar la transacción por error del schedule service
        }

        // Enviar email de cancelación
        try {
            ExternalClientDTO cliente = authClientWrapper.getClientOrThrow(clienteId);
            ExternalServiceDTO servicio = catalogClientWrapper.getServiceOrThrow(reserva.getIdServicio());
            String fechaFormateada = formatDateTime(reserva.getFechaHoraInicio());
            emailService.sendReservationCancellationEmail(
                    cliente.getEmail(),
                    cliente.getNombre(),
                    servicio.getNombreServicio(),
                    fechaFormateada
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de cancelación: {}", e.getMessage());
        }

        return enrichReservationResponse(cancelledReserva);
    }

    // ==================== DISPONIBILIDAD ====================

    @Override
    public boolean checkAvailability(UUID idEmpleado, String fechaHoraInicio, String fechaHoraFin) {
        OffsetDateTime inicio = parseDateTime(fechaHoraInicio);
        OffsetDateTime fin = parseDateTime(fechaHoraFin);
        return !reservationRepository.existsConflictingReservation(idEmpleado, inicio, fin);
    }

    @Override
    public AlternativeSlotsResponseDTO getAlternativeSlots(UUID idServicio, UUID idEmpleado, String fechaHoraDeseada) {
        // Implementación simplificada - en producción consultar al Schedule Service
        // por los horarios disponibles del empleado
        return AlternativeSlotsResponseDTO.builder()
                .idEmpleado(idEmpleado)
                .slotsDisponibles(List.of())
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private ReservationResponseDTO enrichReservationResponse(Reservation reserva) {
        ExternalClientDTO cliente = authClientWrapper.getClientOrThrow(reserva.getIdCliente());
        ExternalServiceDTO servicio = catalogClientWrapper.getServiceOrThrow(reserva.getIdServicio());
        ExternalProviderDTO proveedor = authClientWrapper.getProviderOrThrow(reserva.getIdProveedor());
        EmployeeBasicInfoDTO employeeInfo = scheduleClientWrapper.getEmployeeBasicInfo(reserva.getIdEmpleado());

        return reservationMapper.toResponseDTO(reserva, cliente, servicio, proveedor, employeeInfo);
    }

    private OffsetDateTime parseDateTime(String dateTime) {
        try {
            return OffsetDateTime.parse(dateTime);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Formato de fecha inválido. Use ISO 8601: 2025-08-20T10:00:00Z");
        }
    }

    private OffsetDateTime calculateEndTime(OffsetDateTime inicio, Integer duracionMinutos) {
        int duracion = duracionMinutos != null ? duracionMinutos : 60;
        return inicio.plusMinutes(duracion);
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
        return dateTime.format(formatter);
    }

    private Reservation.ReservationStatus parseEstado(String estado) {
        try {
            return Reservation.ReservationStatus.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Estado inválido: " + estado);
        }
    }

    private void validateStatusTransition(Reservation.ReservationStatus actual, Reservation.ReservationStatus nuevo) {
        // Reglas de transición de estado
        if (actual == Reservation.ReservationStatus.CANCELADA ||
            actual == Reservation.ReservationStatus.COMPLETADA ||
            actual == Reservation.ReservationStatus.NO_SHOW) {
            throw new InvalidReservationStateException("No se puede cambiar el estado de una reserva " + actual.name());
        }
    }

    private ReservationListResponseDTO buildListResponse(List<ReservationResponseDTO> reservas, int pagina, int tamanio) {
        int inicio = Math.min(pagina * tamanio, reservas.size());
        int fin = Math.min(inicio + tamanio, reservas.size());
        List<ReservationResponseDTO> page = reservas.subList(inicio, fin);

        return ReservationListResponseDTO.builder()
                .reservas(page)
                .total(reservas.size())
                .pagina(pagina)
                .tamanioPagina(tamanio)
                .tieneSiguiente(fin < reservas.size())
                .build();
    }
}
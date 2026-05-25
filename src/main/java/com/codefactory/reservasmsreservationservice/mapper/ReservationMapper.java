package com.codefactory.reservasmsreservationservice.mapper;

import com.codefactory.reservasmsreservationservice.dto.external.ExternalClientDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalProviderDTO;
import com.codefactory.reservasmsreservationservice.dto.external.ExternalServiceDTO;
import com.codefactory.reservasmsreservationservice.dto.response.EmployeeBasicInfoDTO;
import com.codefactory.reservasmsreservationservice.dto.response.ReservationResponseDTO;
import com.codefactory.reservasmsreservationservice.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper para convertir entre Reservation entity y DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "idReserva", source = "idReserva")
    @Mapping(target = "idCliente", source = "idCliente")
    @Mapping(target = "idServicio", source = "idServicio")
    @Mapping(target = "idEmpleado", source = "idEmpleado")
    @Mapping(target = "idProveedor", source = "idProveedor")
    @Mapping(target = "fechaHoraInicio", source = "fechaHoraInicio")
    @Mapping(target = "fechaHoraFin", source = "fechaHoraFin")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion")
    @Mapping(target = "fechaCancelacion", source = "fechaCancelacion")
    @Mapping(target = "comentarios", source = "comentarios")
    ReservationResponseDTO toResponseDTO(Reservation reservation);

    default ReservationResponseDTO toResponseDTO(
            Reservation reservation,
            ExternalClientDTO cliente,
            ExternalServiceDTO servicio,
            ExternalProviderDTO proveedor,
            EmployeeBasicInfoDTO employeeInfo) {
        
        ReservationResponseDTO dto = toResponseDTO(reservation);
        
        if (cliente != null) {
            dto.setClienteNombre(cliente.getNombre());
            dto.setClienteEmail(cliente.getEmail());
        }
        
        if (servicio != null) {
            dto.setServicioNombre(servicio.getNombreServicio());
            dto.setDuracionMinutos(servicio.getDuracionMinutos());
        }
        
        if (proveedor != null) {
            dto.setProveedorNombre(proveedor.getNombreComercial());
        }
        
        if (employeeInfo != null) {
            dto.setEmpleadoNombre(employeeInfo.getFullName());
        }
        
        return dto;
    }

    @Mapping(target = "idReserva", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reservation toEntity(ReservationResponseDTO dto);

    void updateFromDTO(ReservationResponseDTO dto, @MappingTarget Reservation entity);
}
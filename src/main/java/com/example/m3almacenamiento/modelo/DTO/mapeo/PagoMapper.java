package com.example.m3almacenamiento.modelo.DTO.mapeo;

import com.example.m3almacenamiento.modelo.DTO.response.PagoResponse;
import com.example.m3almacenamiento.modelo.entidad.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface PagoMapper {
    @Mapping(target = "idPago", ignore = true)
    PagoResponse toResponse(Pago pago);
}

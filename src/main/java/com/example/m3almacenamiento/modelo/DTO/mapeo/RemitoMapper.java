package com.example.m3almacenamiento.modelo.DTO.mapeo;

import com.example.m3almacenamiento.modelo.DTO.response.RemitoResponse;
import com.example.m3almacenamiento.modelo.entidad.Remito;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface RemitoMapper {
    RemitoResponse toResponse(Remito remito);
}

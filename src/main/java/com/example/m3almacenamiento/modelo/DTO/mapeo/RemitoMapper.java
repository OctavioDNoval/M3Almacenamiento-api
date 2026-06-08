package com.example.m3almacenamiento.modelo.DTO.mapeo;

import com.example.m3almacenamiento.modelo.DTO.response.RemitoResponse;
import com.example.m3almacenamiento.modelo.entidad.Remito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface RemitoMapper {
    @Mapping(target = "idRemito", source = "idPublico")
    RemitoResponse toResponse(Remito remito);
}

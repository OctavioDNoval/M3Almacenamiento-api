package com.example.m3almacenamiento.modelo.DTO.mapeo;

import com.example.m3almacenamiento.modelo.DTO.response.LogResponse;
import com.example.m3almacenamiento.modelo.entidad.Log;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LogMapper {

    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "usuarioToNombreCompleto")
    LogResponse toResponse(Log log);

    @Named("usuarioToNombreCompleto")
    default String usuarioToNombreCompleto(Usuario usuario) {
        return usuario != null ? usuario.getNombreCompleto() : "Sistema";
    }
}
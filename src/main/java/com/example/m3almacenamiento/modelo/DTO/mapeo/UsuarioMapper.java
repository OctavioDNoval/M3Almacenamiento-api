package com.example.m3almacenamiento.modelo.DTO.mapeo;

import com.example.m3almacenamiento.modelo.DTO.request.UsuarioRequest;
import com.example.m3almacenamiento.modelo.DTO.response.UsuarioResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_USUARIO;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {LocalDateTime.class}
)
public interface UsuarioMapper {
    // ==========Conversion para Crear (request -> Entity)============

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "estado", constant = "activo")
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "bauleras", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    //==========Conversion para leer (Entity -> Response)==============

    @Mapping(target = "idUsuario", source = "idUsuario")
    @Mapping(target = "fechaCreacion", source = "fechaCreacion", qualifiedByName = "dateToLocalDateTime")
    @Mapping(target = "idBaulera", source = "bauleras", qualifiedByName = "extractBauleraIds")
    @Mapping(target = "nroBaulera", source = "bauleras", qualifiedByName = "extractBauleraNro")
    UsuarioResponse toResponse(Usuario usuario);

    //===============Conversion para actualizar-======================

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "dni", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "bauleras", ignore = true)
    void updateFromRequest(UsuarioRequest request, @MappingTarget Usuario usuario);

    //===========Metodos auxiliares================

    @Named("dateToLocalDateTime")
    default LocalDateTime dateToLocalDateTime(Date date) {
        if(date == null) return null;
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }

    @Named("extractBauleraIds")
    default List<Long> extractBauleraId(java.util.List<Baulera> bauleras) {
        if(bauleras == null || bauleras.isEmpty()) return null;

        return bauleras
                .stream()
                .filter(b -> "ocupada".equals(b.getEstadoBaulera().name()))
                .map(Baulera::getIdBaulera)
                .collect(Collectors.toList());
    }

    @Named("excractBauleraNumero")
    default List<String> excractBauleraNumero(java.util.List<Baulera> bauleras) {
        if(bauleras == null || bauleras.isEmpty()) return null;

        return bauleras
                .stream()
                .filter(b->"ocupada".equals(b.getEstadoBaulera().name()))
                .map(Baulera::getNroBaulera)
                .collect(Collectors.toList());
    }
}

package com.example.m3almacenamiento.modelo.DTO.mapeo;

import com.example.m3almacenamiento.modelo.DTO.request.TipoBauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.TipoBauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoBauleraMapper {

    @Mapping(target = "idTipoBaulera", ignore = true)
    TipoBaulera toEntity(TipoBauleraRequest tipoBauleraRequest);

    TipoBauleraResponse toResponse(TipoBaulera tipoBaulera);

    @Mapping(target = "idTipoBaulera", ignore = true)
    void updateFromRequest(TipoBauleraRequest request, @MappingTarget TipoBaulera tipoBaulera);

    java.util.List<TipoBauleraResponse> toListResponse(java.util.List<TipoBaulera> tipos);

}

package com.example.m3almacenamiento.modelo.DTO.mapeo;

import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {TipoBauleraMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BauleraMapper {

    // =============Conversion para Crear (Request -> Entity)================

    @Mapping(target = "idBaulera", ignore = true)
    @Mapping(target = "estadoBaulera", ignore = true)
    @Mapping(target = "usuarioAsignado", source = "idUsuario", qualifiedByName = "idToUsuario")
    @Mapping(target = "fechaAsignacion", ignore = true)
    @Mapping(target = "tipoBaulera", source = "idTipoBaulera", qualifiedByName = "idToTipoBaulera")
    Baulera toEntity(BauleraRequest bauleraRequest);

    //============Conversion para leer (Entity -> Response)

    @Mapping(target = "tipoBauleraNombre" , source = "tipoBaulera.tipoBauleraNombre")
    @Mapping(target = "tipoBauleraPrecio" , source = "tipoBaulera.precioMensual")
    @Mapping(target = "idUsuario" , source = "usuarioAsignado.idUsuario")
    @Mapping(target = "nombreUsuario", source = "usuarioAsignado.nombreCompleto")
    @Mapping(target = "emailUsuario", source = "usuarioAsignado.email")
    BauleraResponse  toResponse(Baulera baulera);

    //============Conversion para actualizar ================

    @Mapping(target = "idBaulera", ignore = true)
    @Mapping(target = "estadoBaulera", ignore = true)
    @Mapping(target = "usuarioAsignado", ignore = true)
    @Mapping(target = "fechaAsignacion", ignore = true)
    @Mapping(target = "tipoBaulera", source = "idTipoBaulera", qualifiedByName = "idToTipoBaulera")
    void updateFromRequest (BauleraRequest request, @MappingTarget Baulera baulera);

    //============Metodos auxiliares===============

    @Named("idToUsuario")
    default TipoBaulera idToTipoBaulera(Long idTipoBaulera) {
        if(idTipoBaulera == null) return null;

        TipoBaulera tipoBaulera = new TipoBaulera();
        tipoBaulera.setIdTipoBaulera(idTipoBaulera);
        return tipoBaulera;
    }

    @Named("idToUsuario")
    default Usuario idToUsuario(Long idUsuario) {
        if(idUsuario == null) return null;

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        return usuario;
    }

    java.util.List<BauleraResponse> toListResponse(java.util.List<Baulera> bauleras);
}

package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.configuracion.anotaciones.SetAuditUser;
import com.example.m3almacenamiento.modelo.DTO.mapeo.TipoBauleraMapper;
import com.example.m3almacenamiento.modelo.DTO.request.TipoBauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.TipoBauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import com.example.m3almacenamiento.repositorios.TipoBauleraRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.TypeRegistration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TipoBauleraService {
    private final TipoBauleraRepositorio  tipoBauleraRepositorio;
    private final TipoBauleraMapper tipoBauleraMapper;
    private final GestorBauleraService gestorBauleraService;

    @SetAuditUser
    @CacheEvict(value = "dashboard", allEntries = true)
    public TipoBauleraResponse crear(TipoBauleraRequest request){
        if(tipoBauleraRepositorio.existsByTipoBauleraNombre(request.getTipoBauleraNombre())){
            throw new RuntimeException("Tipo de baulera ya existe");
        }

        TipoBaulera tipoBaulera = tipoBauleraMapper.toEntity(request);
        TipoBaulera bauleraGuardada = tipoBauleraRepositorio.save(tipoBaulera);
        return tipoBauleraMapper.toResponse(bauleraGuardada);
    }

    public List<TipoBauleraResponse> obtenerTodos(){
        List<TipoBaulera> tiposBauleras = tipoBauleraRepositorio.findAll();

        return tiposBauleras
                .stream()
                .map(tipoBauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TipoBauleraResponse obtenerPorId(UUID id){
        TipoBaulera tipoBaulera = tipoBauleraRepositorio.findByIdPublico(id)
                .orElseThrow(()-> new RuntimeException("Tipo de baulera no encontrado"));
        return tipoBauleraMapper.toResponse(tipoBaulera);
    }

    @SetAuditUser
    @CacheEvict(value = "dashboard", allEntries = true)
    public boolean eliminarTipoBauleraOnCascade(UUID idTipoBaulera){
        if(!tipoBauleraRepositorio.existsByIdPublico(idTipoBaulera)){
            throw new RuntimeException("Tipo de baulera no encontrado");
        }

        TipoBaulera tipoBaulera = tipoBauleraRepositorio.findByIdPublico(idTipoBaulera).orElseThrow();
        gestorBauleraService.eliminarPorTipoBaulera(tipoBaulera.getIdTipoBaulera());
        tipoBauleraRepositorio.deleteById(tipoBaulera.getIdTipoBaulera());
        return true;
    }

    @SetAuditUser
    @CacheEvict(value = "dashboard", allEntries = true)
    public TipoBauleraResponse actualizarPrecio(Double newPrecio, UUID idTipoBaulera){
        TipoBaulera tb = tipoBauleraRepositorio.findByIdPublico(idTipoBaulera)
                .orElseThrow(()-> new RuntimeException("Tipo de baulera no encontrado"));

        tb.setPrecioMensual(newPrecio);
        TipoBaulera bauleraActualizada = tipoBauleraRepositorio.save(tb);
        return tipoBauleraMapper.toResponse(bauleraActualizada);
    }

}

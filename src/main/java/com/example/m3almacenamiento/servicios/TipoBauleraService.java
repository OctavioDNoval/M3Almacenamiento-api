package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.TipoBauleraMapper;
import com.example.m3almacenamiento.modelo.DTO.request.TipoBauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.TipoBauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import com.example.m3almacenamiento.repositorios.TipoBauleraRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.TypeRegistration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TipoBauleraService {
    private final TipoBauleraRepositorio  tipoBauleraRepositorio;
    private final TipoBauleraMapper tipoBauleraMapper;
    private final GestorBauleraService gestorBauleraService;

    public TipoBauleraResponse crear(TipoBauleraRequest request){
        if(tipoBauleraRepositorio.existsByNombreTipoBaulera(request.getTipoBauleraNombre())){
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

    public TipoBauleraResponse obtenerPorId(Long id){
        TipoBaulera tipoBaulera = tipoBauleraRepositorio.findById(id)
                .orElseThrow(()-> new RuntimeException("Tipo de baulera no encontrado"));
        return tipoBauleraMapper.toResponse(tipoBaulera);
    }

    public void eliminarTipoBauleraOnCascade(Long idTipoBaulera){
        if(!tipoBauleraRepositorio.existsById(idTipoBaulera)){
            throw new RuntimeException("Tipo de baulera no encontrado");
        }

        TipoBaulera tipoBaulera = tipoBauleraRepositorio.findById(idTipoBaulera).orElseThrow();
        gestorBauleraService.eliminarPorTipoBaulera(idTipoBaulera);
        tipoBauleraRepositorio.deleteById(idTipoBaulera);
    }

    public void eliminarTipoBauleraSetNullOnCascade(Long idTipoBaulera){
        if(!tipoBauleraRepositorio.existsById(idTipoBaulera)){
            throw new RuntimeException("Tipo de baulera no encontrado");
        }

        TipoBaulera tipoBaulera = tipoBauleraRepositorio.findById(idTipoBaulera).orElseThrow();
        gestorBauleraService.setNullOnDeleteTipoBaulera(idTipoBaulera);
        tipoBauleraRepositorio.deleteById(idTipoBaulera);
    }
}

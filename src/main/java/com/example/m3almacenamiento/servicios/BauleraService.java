package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.BauleraMapper;
import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.TipoBauleraRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BauleraService {
    private final BauleraRepositorio  bauleraRepositorio;
    private final BauleraMapper bauleraMapper;
    private final TipoBauleraRepositorio tipoBauleraRepositorio;

    public BauleraResponse crear(BauleraRequest bauleraRequest){
        if(bauleraRepositorio.existsByNroBaulera(bauleraRequest.getNroBaulera().trim())){
            throw new RuntimeException("Baulera con NRO: "+bauleraRequest.getNroBaulera()+" ya existe");
        }

        Baulera baulera = bauleraMapper.toEntity(bauleraRequest);
        baulera.setEstadoBaulera(ESTADO_BAULERA.disponible);
        baulera.setFechaAsignacion(new Date());

        Baulera bauleraGuardada =  bauleraRepositorio.save(baulera);
        return bauleraMapper.toResponse(baulera);
    }

    public List<BauleraResponse> obtenerTodos (){
        List<Baulera>  bauleras = bauleraRepositorio.findAll();

        return bauleras
                .stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BauleraResponse obtenerPorId(Long id){
        Baulera baulera = bauleraRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Baulera no encontrada"));

        return bauleraMapper.toResponse(baulera);
    }

    public BauleraResponse actualizar(BauleraRequest request, Long id){
        /*Baulera bauleraExistente = bauleraRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Baulera no encontrada"));

        if(request.getNroBaulera() != null &&
                bauleraRepositorio.existsByNroBaulera(request.getNroBaulera().trim())){
            throw new RuntimeException("Baulera existente");
        }

        Baulera bauleraActualizada = bauleraMapper.toEntity(request);
        */
        return new BauleraResponse();
    }

    public void eliminar (Long id){
        if(!bauleraRepositorio.existsById(id)){
            throw new RuntimeException("Baulera no encontrada");
        }

        Baulera baulera = bauleraRepositorio.findById(id).orElseThrow();
        if(baulera.getUsuarioAsignado()!=null){
            baulera.setUsuarioAsignado(null);
        }
        if(baulera.getTipoBaulera()!=null){
            baulera.setTipoBaulera(null);
        }
        bauleraRepositorio.delete(baulera);
    }

    public void eliminarPorTipoBaulera(Long idTipoBaulera){
        if(!tipoBauleraRepositorio.existsById(idTipoBaulera)){
            throw new RuntimeException("Tipo Baulera no encontrada");
        }

        List<Baulera> baulerasList = bauleraRepositorio.findAll();
        for(Baulera baulera : baulerasList){
            if(baulera.getTipoBaulera().getIdTipoBaulera().equals(idTipoBaulera)){
                bauleraRepositorio.delete(baulera);
            }
        }
    }

    public void setNullOnDeleteTipoBaulera(Long idTipoBaulera){
        if(!tipoBauleraRepositorio.existsById(idTipoBaulera)){
            throw new RuntimeException("Tipo Baulera no encontrada");
        }

        List<Baulera> baulerasList = bauleraRepositorio.findAll();
        for(Baulera baulera : baulerasList){
            if(baulera.getTipoBaulera().getIdTipoBaulera().equals(idTipoBaulera)){
                baulera.setTipoBaulera(null);
            }
        }
    }
}

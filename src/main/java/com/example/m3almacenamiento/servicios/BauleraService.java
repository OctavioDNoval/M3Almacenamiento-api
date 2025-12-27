package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.BauleraMapper;
import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
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
        if(bauleraRequest.getIdUsuario()!=null){
            baulera.setFechaAsignacion(new Date());
        }

        TipoBaulera tipoBaulera = tipoBauleraRepositorio.findById(bauleraRequest.getIdTipoBaulera())
                .orElse(null);
        if(tipoBaulera!=null){
            baulera.setTipoBaulera(tipoBaulera);
        }

        Baulera bauleraGuardada =  bauleraRepositorio.save(baulera);
        return bauleraMapper.toResponse(bauleraGuardada);
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

    public List<BauleraResponse> obtenerPorIdUsuario(Long idUsuario){
        List<Baulera> listaBauleras = bauleraRepositorio.findByUsuarioAsignado_IdUsuario(idUsuario);
        return listaBauleras
                .stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    public boolean eliminar (Long id){
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
        return true;
    }
}

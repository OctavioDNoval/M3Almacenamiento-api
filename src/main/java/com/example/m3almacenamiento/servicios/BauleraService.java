package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.BauleraMapper;
import com.example.m3almacenamiento.modelo.DTO.request.BauleraRequest;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
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
        Baulera bauleraExistente = bauleraRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Baulera no encontrada"));

        if(request.getNroBaulera() != null )
    }
}

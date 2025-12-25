package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.TipoBauleraRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GestorBauleraService {
    private final TipoBauleraRepositorio tipoBauleraRepositorio;
    private final BauleraRepositorio bauleraRepositorio;

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

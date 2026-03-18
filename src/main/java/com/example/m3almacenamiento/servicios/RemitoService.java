package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.InfoDeudaEmail;


import com.example.m3almacenamiento.modelo.entidad.Remito;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.RemitoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RemitoService {
    private final RemitoRepositorio remitoRepositorio;
    private final EmailService emailService;

    public void generarRemito(Usuario usuario, InfoDeudaEmail infoDeudaEmail){
        Remito remito = new Remito();
        remito.setUsuario(usuario);
        remito.setPeriodo(emailService.getNombreMesActual());
        remito.setFechaEmision(LocalDate.now());
        remito.setImporteTotal(infoDeudaEmail.getTotalCalculado());

        String bauleras = "";
        List<String> listaBauleras = infoDeudaEmail.getNumerosBauleras();
        for(int i = 0; i < listaBauleras.size(); i++){
            bauleras = bauleras.concat(listaBauleras.get(i));  
            if(i != listaBauleras.size() - 1){
                bauleras = bauleras.concat(",");  
            }
        }

        remito.setBaulerasString(bauleras);
        remitoRepositorio.save(remito);
    }
}

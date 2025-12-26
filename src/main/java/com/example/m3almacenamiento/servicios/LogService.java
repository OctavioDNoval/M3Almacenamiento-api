package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.entidad.Log;
import com.example.m3almacenamiento.repositorios.LogRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepositorio logRepositorio;

    public List<Log> obtenerTodos (){
        List<Log> logs = logRepositorio.findAll();
        return logs;
    }
}

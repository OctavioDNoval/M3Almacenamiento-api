package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.repositorios.RemitoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemitoService {
    private final RemitoRepositorio remitoRepositorio;
}

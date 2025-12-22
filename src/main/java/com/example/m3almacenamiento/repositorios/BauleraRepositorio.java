package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Baulera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BauleraRepositorio extends JpaRepository<Long, Baulera> {
}

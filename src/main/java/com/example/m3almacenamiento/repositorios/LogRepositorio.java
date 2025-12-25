package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepositorio extends JpaRepository<Log,Long>{
}

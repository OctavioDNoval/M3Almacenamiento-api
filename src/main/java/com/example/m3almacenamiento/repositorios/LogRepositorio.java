package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LogRepositorio extends JpaRepository<Log,Long>{
    List<Log> findTop15ByAccionOrderByFechaDesc(String accion);
    Optional<Log> findByIdPublico(UUID id);
}

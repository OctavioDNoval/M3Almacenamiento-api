package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.TipoBaulera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoBauleraRepositorio extends JpaRepository<TipoBaulera, Long> {
    Boolean existsByTipoBauleraNombre(String nombreTipoBaulera);
    Optional<TipoBaulera> findByIdPublico(UUID id);
}

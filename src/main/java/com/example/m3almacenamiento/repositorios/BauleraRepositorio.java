package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Baulera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BauleraRepositorio extends JpaRepository<Baulera, Long> {
    Boolean existsByNroBaulera(String nroBaulera);
    List<Baulera> findByUsuarioAsignado_IdUsuario(Long idUsuario);

    @Query(value = "select max(cast(b.nroBaulera as integer)) from Baulera b")
    Integer findMaxNroBauleraAsInteger();
}

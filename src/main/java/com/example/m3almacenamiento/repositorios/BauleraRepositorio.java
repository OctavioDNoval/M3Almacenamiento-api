package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BauleraRepositorio extends JpaRepository<Baulera, Long> {
    Boolean existsByNroBaulera(String nroBaulera);
    List<Baulera> findByUsuarioAsignado_IdUsuario(Long idUsuario);
    Boolean existsBynroBaulera(String nroBaulera);

    @Query(value = "SELECT * FROM baulera b WHERE b.estado_baulera = 'disponible'", nativeQuery = true)
    List<Baulera> findAllDisponible();

    @Query(value = "select max(cast(b.nroBaulera as integer)) from Baulera b")
    Optional<Integer> findMaxNroBauleraAsInteger();

    List<Baulera> findByDiaVencimientoPago(Integer diaVencimientoPago);

    @Query("SELECT b FROM Baulera b WHERE " +
            "b.nroBaulera LIKE %:search% OR " +
            "b.usuarioAsignado.nombreCompleto LIKE %:search% OR " +
            "b.tipoBaulera.tipoBauleraNombre LIKE %:search%")
    Page<Baulera> findBySearch(@Param("search") String search, Pageable pageable);

}

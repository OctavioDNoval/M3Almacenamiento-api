package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.DTO.response.PagoResponse;
import com.example.m3almacenamiento.modelo.entidad.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PagoRepositorio extends JpaRepository<Pago, Long> {
    @Query("SELECT p FROM Pago p WHERE " +
            "LOWER(p.usuario.nombreCompleto) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.usuario.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Pago> findBySearch(@Param("search") String search, Pageable pageable);

    List<Pago> getPagoByUsuario_IdPublico(UUID usuarioIdPublico);
}

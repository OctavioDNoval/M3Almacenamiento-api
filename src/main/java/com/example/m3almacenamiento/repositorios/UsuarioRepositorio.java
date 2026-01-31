package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Boolean existsByEmail(String email);
    Boolean existsByDni(String dni);

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE " +
            "LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) ")
    Page<Usuario> findBySearch(@Param("search") String search, Pageable pageable);

}

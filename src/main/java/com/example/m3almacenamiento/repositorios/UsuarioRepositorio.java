package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Boolean existsByEmail(String email);
    Boolean existsByDni(String dni);

    Optional<Usuario> findByEmail(String email);
}

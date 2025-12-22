package com.example.m3almacenamiento.repositorios;

import com.example.m3almacenamiento.modelo.entidad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepositorio extends JpaRepository<Long, Usuario> {
}

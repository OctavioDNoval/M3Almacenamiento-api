package com.example.m3almacenamiento.configuracion;

import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("preSecurityService")
public class PreSecurityService {

    private UsuarioRepositorio usuarioRepositorio;

    public Boolean esMismoUsuario(Long idUsuario){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return usuarioRepositorio.findByEmail(email)
                .map(u -> u.getIdUsuario().equals(idUsuario))
                .orElse(false);
    }
}

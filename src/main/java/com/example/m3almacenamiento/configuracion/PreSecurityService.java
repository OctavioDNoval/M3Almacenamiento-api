package com.example.m3almacenamiento.configuracion;

import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("preSecurityService")
public class PreSecurityService {

    private UsuarioRepositorio usuarioRepositorio;

    public PreSecurityService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Boolean esMismoUsuario(UUID idUsuario){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return usuarioRepositorio.findByEmail(email)
                .map(u -> u.getIdPublico().equals(idUsuario))
                .orElse(false);
    }
}

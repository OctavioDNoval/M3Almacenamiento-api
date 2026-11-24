package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.configuracion.JwtService;
import com.example.m3almacenamiento.modelo.DTO.auth.AuthResponse;
import com.example.m3almacenamiento.modelo.DTO.auth.LoginRequest;
import com.example.m3almacenamiento.modelo.DTO.mapeo.UsuarioMapper;
import com.example.m3almacenamiento.modelo.DTO.response.UsuarioResponse;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;


    public AuthResponse login(LoginRequest loginRequest) {
        // Same error in every case so the login does not reveal which emails exist
        Usuario usuario = usuarioRepositorio.findByEmail(loginRequest.getEmail())
                .orElseThrow(AuthService::credencialesInvalidas);

        // Clients are only records: just admins and semiadmins can sign in
        if(usuario.getRol() == ROL.USER){
            throw credencialesInvalidas();
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPasswordHash())){
            throw credencialesInvalidas();
        }

        String token = jwtService.generateToken(
                User
                        .withUsername(usuario.getEmail())
                        .password(usuario.getPasswordHash())
                        .roles(usuario.getRol().name())
                        .build()
        );
        UsuarioResponse usuarioResponse = usuarioMapper.toResponse(usuario);
        return new AuthResponse(token, usuarioResponse);
    }

    private static BadCredentialsException credencialesInvalidas() {
        return new BadCredentialsException("Email o contraseña incorrectos");
    }
}

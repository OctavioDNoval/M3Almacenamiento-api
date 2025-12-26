package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.configuracion.JwtService;
import com.example.m3almacenamiento.modelo.DTO.auth.AuthResponse;
import com.example.m3almacenamiento.modelo.DTO.auth.LoginRequest;
import com.example.m3almacenamiento.modelo.DTO.mapeo.UsuarioMapper;
import com.example.m3almacenamiento.modelo.DTO.request.UsuarioRequest;
import com.example.m3almacenamiento.modelo.DTO.response.UsuarioResponse;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
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
        Usuario usuario = usuarioRepositorio.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if(!passwordEncoder.matches(loginRequest.getPassword(), loginRequest.getPassword())){
            throw new RuntimeException("Constrasenia incorrecta");
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

    public AuthResponse register(UsuarioRequest usuarioRequest) {
        if(usuarioRepositorio.existsByEmail(usuarioRequest.getEmail())){
            throw new RuntimeException("Usuario existente");
        }
        Usuario usuario = usuarioMapper.toEntity(usuarioRequest);
        usuario.setPasswordHash(passwordEncoder.encode(usuarioRequest.getPassword()));
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);
        String token = jwtService.generateToken(
                User.withUsername(usuarioGuardado.getEmail())
                        .password(usuarioGuardado.getPasswordHash())
                        .roles(ROL.USER.name())
                        .build()
        );
        UsuarioResponse usuarioResponse = usuarioMapper.toResponse(usuario);
        return new AuthResponse(token, usuarioResponse);
    }
}

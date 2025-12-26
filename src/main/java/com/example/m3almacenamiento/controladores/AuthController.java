package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.configuracion.JwtService;
import com.example.m3almacenamiento.modelo.DTO.auth.AuthResponse;
import com.example.m3almacenamiento.modelo.DTO.auth.LoginRequest;
import com.example.m3almacenamiento.modelo.DTO.request.UsuarioRequest;
import com.example.m3almacenamiento.servicios.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(UsuarioRequest usuarioRequest) {
        return ResponseEntity.ok(authService.register(usuarioRequest));
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader("Authorization") String token) {
        try{
            String cleanToken = token.replace("Bearer ", "");
            if(jwtService.isTokenValid(cleanToken)){
                return ResponseEntity.ok(jwtService.refreshToken(cleanToken));
            }else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token invalido "+e.getMessage());
        }
    }
}

package com.example.m3almacenamiento.controladores;

import com.example.m3almacenamiento.configuracion.JwtService;
import com.example.m3almacenamiento.modelo.DTO.auth.AuthResponse;
import com.example.m3almacenamiento.modelo.DTO.auth.LoginRequest;
import com.example.m3almacenamiento.modelo.DTO.request.UsuarioRequest;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
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
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UsuarioRequest usuarioRequest) {
        System.out.println("=== REGISTRANDO NUEVO USUARIO ===");
        System.out.println("Email: " + usuarioRequest.getEmail());
        System.out.println("DNI: " + usuarioRequest.getDni());
        System.out.println("Password recibida: " + usuarioRequest.getPassword());
        System.out.println("Rol en Request: " + usuarioRequest.getRol());
        return ResponseEntity.ok(authService.register(usuarioRequest));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String token) {
        try{
            String cleanToken = token.replace("Bearer ", "").trim();
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

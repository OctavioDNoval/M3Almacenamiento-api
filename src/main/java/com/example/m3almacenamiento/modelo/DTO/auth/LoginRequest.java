package com.example.m3almacenamiento.modelo.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;
    @NotBlank(message = "La contrasenia es obligatoria")
    private String password;
}

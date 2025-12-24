package com.example.m3almacenamiento.modelo.DTO.request;

import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioRequest {
    @Pattern(regexp = "\\d{8,15}", message = "El DNI debe contener numeros solamente")
    private String dni;

    @Size(min = 3, max = 100)
    private String nombreCompleto;

    @Email
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Teléfono inválido")
    private String telefono;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Builder.Default
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ROL rol = ROL.USER;
}

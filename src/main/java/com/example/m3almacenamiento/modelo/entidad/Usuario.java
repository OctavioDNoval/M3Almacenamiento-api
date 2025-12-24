package com.example.m3almacenamiento.modelo.entidad;

import com.example.m3almacenamiento.modelo.enumerados.ESTADO_USUARIO;
import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios",
    indexes = {
        @Index(name = "idx_dni", columnList = "dni"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_estado", columnList = "estado")
    }
)
@Builder
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"bauleras"})
@EqualsAndHashCode(exclude = {"bauleras"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank
    private String dni;

    @Column(nullable = false, name = "nombre_completo")
    @NotBlank
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    @Email(message = "El email debe tener formato valido")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Teléfono inválido")
    private String telefono;

    @Column(nullable = false,name = "password_hash")
    @JsonIgnore
    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ROL rol = ROL.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ESTADO_USUARIO estado = ESTADO_USUARIO.activo;

    @CreatedDate
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @OneToMany(mappedBy = "usuarioAsignado", fetch = FetchType.LAZY)
    private List<Baulera> bauleras = new ArrayList<>();
}

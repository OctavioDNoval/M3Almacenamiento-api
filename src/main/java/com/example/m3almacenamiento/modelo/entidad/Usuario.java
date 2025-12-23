package com.example.m3almacenamiento.modelo.entidad;

import com.example.m3almacenamiento.modelo.enumerados.ESTADO_USUARIO;
import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

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
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false)
    private String nombre_completo;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;

    @Column(nullable = false)
    @JsonIgnore
    private String password_hash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ROL rol = ROL.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ESTADO_USUARIO estado = ESTADO_USUARIO.activo;

    @CreatedDate
    private Date fecha_creacion;
}

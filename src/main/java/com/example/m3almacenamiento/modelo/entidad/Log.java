package com.example.m3almacenamiento.modelo.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "logs")
@EntityListeners(AuditingEntityListener.class)
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_log;

    @ManyToOne
    private Usuario usuario;

    private String accion;

    private String tabla_afectada;

    private String descripcion;

    private String valores_anteriores;

    private String valores_nuevos;

    @CreatedDate
    private Date fecha;
}

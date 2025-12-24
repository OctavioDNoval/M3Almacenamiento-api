package com.example.m3almacenamiento.modelo.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "logs")
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario")
    private Usuario usuario;

    private String accion;

    @Column(name = "tabla_afectada")
    private String tablaAfectada;

    private String descripcion;

    @Column(name = "valores_anteriores", columnDefinition = "TEXT")
    @Lob
    private String valoresAnteriores;

    @Column(name = "valores_nuevos", columnDefinition = "TEXT")
    @Lob
    private String valoresNuevos;

    @CreatedDate
    private LocalDateTime fecha;
}

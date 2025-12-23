package com.example.m3almacenamiento.modelo.entidad;

import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "baulera")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Baulera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_baulera;

    @Column(nullable = false)
    private String NRO_baulera;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ESTADO_BAULERA estado_baulera = ESTADO_BAULERA.disponible;

    @ManyToOne
    @JoinColumn(name = "usuario_asignado", nullable = false)
    private Usuario usuario_asignado;

    @LastModifiedDate
    private Date fecha_asignacion;

    private Integer dia_vencimiento_pago;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "tipo_baulera")
    private TipoBaulera tipo_baulera;
}

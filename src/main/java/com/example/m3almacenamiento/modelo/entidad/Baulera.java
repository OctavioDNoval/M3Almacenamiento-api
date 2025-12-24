package com.example.m3almacenamiento.modelo.entidad;

import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @Column(name = "id_baulera")
    private Long idBaulera;

    @Column(nullable = false, name = "NRO_baulera")
    private String nroBaulera;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "estado_baulera")
    @Builder.Default
    private ESTADO_BAULERA estadoBaulera = ESTADO_BAULERA.disponible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asignado")
    private Usuario usuarioAsignado;

    @Column(name = "fecha_asignacion")
    private Date fechaAsignacion;

    @Min(value = 1, message = "El día de vencimiento debe ser entre 1 y 31")
    @Max(value = 31, message = "El día de vencimiento debe ser entre 1 y 31")
    @Column(name = "dia_vencimiento_pago")
    private Integer diaVencimientoPago;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_baulera")
    @NotNull(message = "El tipo de baulera es obligatorio")
    private TipoBaulera tipoBaulera;
}

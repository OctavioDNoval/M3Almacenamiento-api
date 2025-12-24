package com.example.m3almacenamiento.modelo.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tipobaulera")
@Builder
public class TipoBaulera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_baulera")
    private Long idTipoBaulera;

    private String nombre;

    private String descripcion;

    @Column(name = "precio_mensual")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double precioMensual;
}

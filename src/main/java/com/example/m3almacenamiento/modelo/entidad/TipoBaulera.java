package com.example.m3almacenamiento.modelo.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tipobaulera")
public class TipoBaulera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_tipo_baulera;


    private String nombre;

    private String descripcion;

    private Double precio_mensual;
}

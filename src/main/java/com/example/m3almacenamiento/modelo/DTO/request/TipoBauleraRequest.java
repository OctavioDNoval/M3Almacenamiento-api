package com.example.m3almacenamiento.modelo.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TipoBauleraRequest {
    private String tipoBauleraNombre;
    private String descripcion;
    private Double precioMensual;
}

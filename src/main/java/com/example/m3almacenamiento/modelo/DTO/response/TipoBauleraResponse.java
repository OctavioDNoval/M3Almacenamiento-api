package com.example.m3almacenamiento.modelo.DTO.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TipoBauleraResponse {
    private Long idTipoBaulera;
    private String tipoBauleraNombre;
    private String descripcion;
    private Double precioMensual;

    public String getPrecioFormateado() {
        return String.format("$%.2f", precioMensual);
    }
}

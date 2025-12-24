package com.example.m3almacenamiento.modelo.DTO.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BauleraRequest {
    private String nroBaulera;

    private Long idTipoBaulera;

    private Long idUsuario;

    @Min(value = 1, message = "El día de vencimiento debe ser entre 1 y 31")
    @Max(value = 31, message = "El día de vencimiento debe ser entre 1 y 31")
    private Integer diaVencimientoPago;

    @Size(max = 500)
    private String observaciones;
}

package com.example.m3almacenamiento.modelo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemitoResponse {
    private Long idRemito;
    private String periodo;
    private LocalDate fechaEmision;
    private BigDecimal importeTotal;
    private String datosJson;

    private UsuarioResponse usuario;
}

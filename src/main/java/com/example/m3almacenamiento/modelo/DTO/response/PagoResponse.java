package com.example.m3almacenamiento.modelo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoResponse {
    private UsuarioResponse usuario;
    private BigDecimal montoPagado;
    private LocalDate fechaPago;
}

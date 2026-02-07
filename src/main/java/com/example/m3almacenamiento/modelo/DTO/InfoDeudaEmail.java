package com.example.m3almacenamiento.modelo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class InfoDeudaEmail {
    private BigDecimal montoMensual;
    private BigDecimal nuevaDeudaTotaL;
    private List<String> numerosBauleras;
    private BigDecimal totalCalculado;
    private BigDecimal deudaAnterior;
}

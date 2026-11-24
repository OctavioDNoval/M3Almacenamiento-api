package com.example.m3almacenamiento.modelo.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/*
 * Remito armado a mano desde el panel de admin.
 * Los valores sugeridos (bauleras, deuda, monto) los calcula el front; acá llegan ya confirmados.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemitoManualRequest {
    private UUID idUsuario;

    // Formato yyyy-MM (lo que devuelve <input type="month">)
    private String periodo;

    // Números de baulera (nroBaulera)
    private List<String> bauleras;

    private BigDecimal deudaAnterior;

    // Importe correspondiente al período
    private BigDecimal montoMes;

    private boolean guardar;
}

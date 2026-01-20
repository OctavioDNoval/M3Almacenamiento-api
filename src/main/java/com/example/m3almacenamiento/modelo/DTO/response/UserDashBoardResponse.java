package com.example.m3almacenamiento.modelo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashBoardResponse {

    private ResumenUsuario resumenUsuario;
    private InformacionBauleras informacionBauleras;
    private List<BauleraResponse> bauleras;
    private LocalDateTime fechaCreacion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenUsuario{
        private BigDecimal deudaAcumulada;
        private String mensajeEstado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InformacionBauleras{
        private Integer cantidadTotalBauleras;
        private BigDecimal valorMensualTotal;
        private Map<String,BigDecimal> valorPorTipo;
        private Map<String,Integer> cantidadPorTipo;
    }
}

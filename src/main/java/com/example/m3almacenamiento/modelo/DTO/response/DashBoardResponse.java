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
public class DashBoardResponse {

    private ResumenGeneral resumenGeneral;
    private EstadisticasBauleras estadisticasBauleras;
    private EstadisticasUsuarios estadisticasUsuarios;
    private List<DatoGrafico> ocupacionPorMes;
    private List<DatoGrafico> distribucionTipoBaulera;
    private List<DatoGrafico> ingresosPorTipoBaulera;
    private LocalDateTime fechaGeneracion;

    /*
    * Resumen general del sistema
    * */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenGeneral{
        private Long totalBauleras;
        private Long totalUsuarios;
        private BigDecimal valorMensualTotal;
        private BigDecimal deudaTotalAcumulada;
        private Long BaulerasOcupadas;
        private Long baulerasDisponibles;
        private Integer nuevoUsuarioMes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasBauleras{
        private Long totalBauleras;
        private Long baulerasOcupadas;
        private Long baulerasDisponibles;
        private BigDecimal porcentajeOcupacion;
        private BigDecimal valorMensualTotalOcupadas;
        private Map<String, Long> cantidadPorTipo; // TipoBaulera -> Cantidad
        private Map<String, BigDecimal> valorMensualPorTipo; // TipoBaulera -> Valor Total
        private TipoBauleraMasPopular tipoBauleraMasPopular;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoBauleraMasPopular {
        private String nombre;
        private Long cantidad;
        private BigDecimal porcentaje;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstadisticasUsuarios {
        private Long totalUsuarios;
        private Long usuariosActivos;
        private Long usuarioInactivos;
        private Long usuariosConDeuda;
        private BigDecimal deudaTotal;
        private UsuarioConMayorDeuda usuarioConMayorDeuda;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioConMayorDeuda {
        private String nombre;
        private String email;
        private BigDecimal deuda;
        private Long cantidadBauleras;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatoGrafico {
        private String etiqueta;
        private BigDecimal valor;
        private String color;
    }
}



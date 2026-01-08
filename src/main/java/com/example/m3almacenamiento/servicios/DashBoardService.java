package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.response.DashBoardResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashBoardService {
    private final UsuarioRepositorio  usuarioRepositorio;
    private final BauleraRepositorio  bauleraRepositorio;

    /*===============METODO PARA GENERAR EL DASHBOARD=====================*/
    

    /*===============CLASES AUXILIARES=====================*/

    @RequiredArgsConstructor
    @Data
    private static class EstadisticasBaulerasCalculadas{
        private final DashBoardResponse.EstadisticasBauleras estadisticas;
        private final Long total;
        private final Long ocupadas;
        private final BigDecimal valorMensualOcupadas;

    }

    @Data
    @RequiredArgsConstructor
    private static class EstadisticasUsuariosCalculadas{
        private final DashBoardResponse.EstadisticasUsuarios estadisticas;
        private final Long total;
        private final BigDecimal deudaTotal;
    }

    /*=================METODOS DE CALCULO PRINCIPOALES=======================*/
    private EstadisticasBaulerasCalculadas calcularEstadisticasBauleras(List<Baulera> bauleras){
        long total = bauleras.size();
        long ocupadas = contarBaulerasOcupadas(bauleras);
        long disponibles = total-ocupadas;
        BigDecimal porcentajeOcupacion = calcularPorcentaje(total,ocupadas);
        BigDecimal valorMensualOcupadas = calcularValorMensualOcupadas(bauleras);
        Map<String,Long> cantidadPorTipo = agruparCantidadPorTipo(bauleras);
        Map<String,BigDecimal> valorPorTipo = agruparValorPorTipo(bauleras);
        DashBoardResponse.TipoBauleraMasPopular bauleraMasPopular= encontrarTipoMasPopular(cantidadPorTipo,total);

        DashBoardResponse.EstadisticasBauleras estadisticas =
                DashBoardResponse.EstadisticasBauleras.builder()
                        .totalBauleras(total)
                        .baulerasOcupadas(ocupadas)
                        .baulerasDisponibles(disponibles)
                        .porcentajeOcupacion(porcentajeOcupacion)
                        .valorMensualTotalOcupadas(valorMensualOcupadas)
                        .cantidadPorTipo(cantidadPorTipo)
                        .valorMensualPorTipo(valorPorTipo)
                        .tipoBauleraMasPopular(bauleraMasPopular)
                        .build();

        return new EstadisticasBaulerasCalculadas(estadisticas,total,ocupadas,valorMensualOcupadas);
    }

    /*=================METODOS DE CALUCLO AUXILIARES===========================*/
    private long contarBaulerasOcupadas(List<Baulera> bauleras){
        return bauleras.stream()
                .filter(b -> b.getEstadoBaulera() == ESTADO_BAULERA.ocupada)
                .count();
    }

    private BigDecimal calcularPorcentaje(long total, long aCalcular){
        if(total == 0) return BigDecimal.ZERO;
        return new BigDecimal(aCalcular)
                .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calcularValorMensualOcupadas (List<Baulera> bauleras){
        return bauleras.stream()
                .filter(b -> b.getEstadoBaulera() == ESTADO_BAULERA.ocupada)
                .map(b-> BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String,Long> agruparCantidadPorTipo(List<Baulera> bauleras){
        return bauleras.stream()
                .collect(Collectors.groupingBy(
                        b-> b.getTipoBaulera().getTipoBauleraNombre(),
                        Collectors.counting()
                ));
    }

    private Map<String,BigDecimal> agruparValorPorTipo(List<Baulera> bauleras){
        return bauleras.stream()
                .filter(b-> b.getEstadoBaulera() == ESTADO_BAULERA.ocupada)
                .collect(Collectors.groupingBy(
                        b-> b.getTipoBaulera().getTipoBauleraNombre(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                b -> BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual()),
                                BigDecimal::add
                        )
                ));
    }

    private DashBoardResponse.TipoBauleraMasPopular encontrarTipoMasPopular(
            Map<String,Long> cantidadPorTipo, long totalBauleras
    ){
        if(cantidadPorTipo.isEmpty()) return null;

        Map.Entry<String, Long> masPopular = cantidadPorTipo
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if(masPopular == null) return null;

        BigDecimal porcentaje = calcularPorcentaje(totalBauleras, masPopular.getValue());

        return DashBoardResponse.TipoBauleraMasPopular
                .builder()
                .nombre(masPopular.getKey())
                .cantidad(masPopular.getValue())
                .porcentaje(porcentaje)
                .build();
    }
}

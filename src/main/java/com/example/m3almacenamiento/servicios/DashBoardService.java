package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.BauleraMapper;
import com.example.m3almacenamiento.modelo.DTO.response.BauleraResponse;
import com.example.m3almacenamiento.modelo.DTO.response.DashBoardResponse;
import com.example.m3almacenamiento.modelo.DTO.response.UserDashBoardResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import com.example.m3almacenamiento.modelo.enumerados.ESTADO_USUARIO;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashBoardService {
    private final UsuarioRepositorio  usuarioRepositorio;
    private final BauleraRepositorio  bauleraRepositorio;
    private final BauleraMapper bauleraMapper;

    /*===============METODO PARA GENERAR EL DASHBOARD=====================*/
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "dashboard", key = "'dashboardData'", unless = "#result == null")
    public DashBoardResponse obtenerDashBoard() {
        log.info("Generando DashBoard...");

        List<Baulera> bauleras = bauleraRepositorio.findAll();
        List<Usuario> usuarios = usuarioRepositorio.findAll();

        EstadisticasBaulerasCalculadas statsBauleras = calcularEstadisticasBauleras(bauleras);
        EstadisticasUsuariosCalculadas statsUsuarios = calcularEstadisticasUsuarios(usuarios, bauleras);

        return DashBoardResponse.builder()
                .resumenGeneral(construirResumenGeneral(statsBauleras,statsUsuarios,usuarios))
                .estadisticasBauleras(statsBauleras.getEstadisticas())
                .estadisticasUsuarios(statsUsuarios.getEstadisticas())
                .ocupacionPorMes(calcularOcupacionPorMes(bauleras))
                .distribucionTipoBaulera(calcularDistribucionPorTipo(bauleras))
                .ingresosPorTipoBaulera(calcularIngresosPorTipo(bauleras))
                .fechaGeneracion(LocalDateTime.now())
                .build();
    }

    @PreAuthorize("@preSecurityService.esMismoUsuario(#usuarioId)")
    public UserDashBoardResponse obtenerUserDashBoard(Long usuarioId) {
        log.info("Obtendendo Dashboard para el usuario {}", usuarioId);

        Usuario usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Baulera> baulerasUsuario = obtenerBauleras(usuarioId);
        EstadisticasBaulerasUsuario statsBauleras = calcularEstadisticasBaulerasUsuario(baulerasUsuario);
        List<BauleraResponse> baulerasResponse = convertirBauleraAResponse(baulerasUsuario);

        return UserDashBoardResponse.builder()
                .resumenUsuario(construirResumen(usuario))
                .informacionBauleras(statsBauleras.informacion)
                .bauleras(baulerasResponse)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

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

    @Data
    @RequiredArgsConstructor
    private static class EstadisticasBaulerasUsuario{
        private final UserDashBoardResponse.InformacionBauleras informacion;
        private final Integer cantidadTotal;
        private final BigDecimal valorMensualTotal;
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

    private EstadisticasUsuariosCalculadas calcularEstadisticasUsuarios(List<Usuario> usuarios, List<Baulera> bauleras){
        long total = usuarios.size();
        long activos = contarUsuariosActivos(usuarios);
        long inactivos = total - activos;
        long conDeuda = contarUsuariosConDeuda(usuarios);
        BigDecimal deudaTotal = calcularDeudaTotal(usuarios);
        DashBoardResponse.UsuarioConMayorDeuda usuarioConMayorDeuda =
                encontrarUsuarioConMayorDeuda(usuarios,bauleras);

        DashBoardResponse.EstadisticasUsuarios estadisticas  =
                DashBoardResponse.EstadisticasUsuarios.builder()
                        .totalUsuarios(total)
                        .usuariosActivos(activos)
                        .usuariosInactivos(inactivos)
                        .usuariosConDeuda(conDeuda)
                        .deudaTotal(deudaTotal)
                        .usuarioConMayorDeuda(usuarioConMayorDeuda)
                        .build();

        return new EstadisticasUsuariosCalculadas(estadisticas,total,deudaTotal);
    }

    private DashBoardResponse.ResumenGeneral construirResumenGeneral(
            EstadisticasBaulerasCalculadas statsBauleras,
            EstadisticasUsuariosCalculadas statsUsuarios,
            List<Usuario> usuarios
    ){
        int nuevosUsuariosEsteMes = contarNuevosUsuariosEsteMes(usuarios);

        return DashBoardResponse.ResumenGeneral.builder()
                .totalBauleras(statsBauleras.getTotal())
                .totalUsuarios(statsUsuarios.getTotal())
                .valorMensualTotal(statsBauleras.getValorMensualOcupadas())
                .deudaTotalAcumulada(statsUsuarios.getDeudaTotal())
                .BaulerasOcupadas(statsBauleras.getOcupadas())
                .baulerasDisponibles(statsBauleras.getEstadisticas().getBaulerasDisponibles())
                .nuevoUsuarioMes(nuevosUsuariosEsteMes)
                .build();
    }

    private EstadisticasBaulerasUsuario calcularEstadisticasBaulerasUsuario(List<Baulera> bauleras){
        int cantidadTotal = bauleras.size();
        BigDecimal valorMensualTotal = calcularValorMensualUsuario(bauleras);
        Map<String, BigDecimal> valorPorTipo = agruparValorPorTipo(bauleras);
        Map<String, Integer> cantidadPorTipo = agruparCantidadPorTipoUsuario(bauleras);

        UserDashBoardResponse.InformacionBauleras informacion =
                UserDashBoardResponse.InformacionBauleras.builder()
                        .cantidadTotalBauleras(cantidadTotal)
                        .valorMensualTotal(valorMensualTotal)
                        .valorPorTipo(valorPorTipo)
                        .cantidadPorTipo(cantidadPorTipo)
                        .build();

        return new EstadisticasBaulerasUsuario(informacion,cantidadTotal,valorMensualTotal);
    }

    /*=================METODOS DE CALUCLO AUXILIARES===========================*/
    private List<Baulera> obtenerBauleras(Long idUsuario){
        return bauleraRepositorio.findByUsuarioAsignado_IdUsuario(idUsuario);
    }

    private BigDecimal calcularValorMensualUsuario(List<Baulera> bauleras){
        return bauleras.stream()
                .map(b-> BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, Integer> agruparCantidadPorTipoUsuario(List<Baulera> bauleras){
        return bauleras.stream()
                .collect(Collectors.groupingBy(
                        b-> b.getTipoBaulera().getTipoBauleraNombre(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private List<BauleraResponse> convertirBauleraAResponse(List<Baulera> bauleras){
        return bauleras.stream()
                .map(bauleraMapper::toResponse)
                .collect(Collectors.toList());
    }

    private UserDashBoardResponse.ResumenUsuario construirResumen(Usuario usuario){
        String mensajeEstado = determinarMensajeEstado((usuario.getDeudaAcumulada()));

        return UserDashBoardResponse.ResumenUsuario.builder()
                .deudaAcumulada(usuario.getDeudaAcumulada())
                .mensajeEstado(mensajeEstado)
                .build();
    }

    private String determinarMensajeEstado(BigDecimal deuda) {
        if (deuda == null || deuda.compareTo(BigDecimal.ZERO) == 0) {
            return "✅ Sin deuda";
        } else if (deuda.compareTo(new BigDecimal("50000")) < 0) {
            return "⚠️ Deuda baja";
        } else if (deuda.compareTo(new BigDecimal("150000")) < 0) {
            return "⚠️ Deuda moderada";
        } else {
            return "🚨 Deuda alta - Contactar administración";
        }
    }

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

    private long contarUsuariosActivos(List<Usuario> usuarios){
        return usuarios.stream()
                .filter(u-> u.getEstado() == ESTADO_USUARIO.activo)
                .count();
    }

    private long contarUsuariosConDeuda(List<Usuario> usuarios){
        return usuarios.stream()
                .filter(u -> u.getDeudaAcumulada() != null &&
                        u.getDeudaAcumulada().compareTo(BigDecimal.ZERO) > 0)
                .count();
    }

    private BigDecimal calcularDeudaTotal(List<Usuario> usuarios){
        return usuarios.stream()
                .map(Usuario::getDeudaAcumulada)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DashBoardResponse.UsuarioConMayorDeuda encontrarUsuarioConMayorDeuda(List<Usuario> usuarios, List<Baulera> bauleras){
        Optional<Usuario> usuarioConMayorDeuda = usuarios.stream()
                .filter(u-> u.getDeudaAcumulada() !=null)
                .max(Comparator.comparing(Usuario::getDeudaAcumulada));

        if(!usuarioConMayorDeuda.isPresent()) return null;

        Usuario usuario = usuarioConMayorDeuda.get();
        long cantidadBauleras = contarBaulerasDeUsuario(usuario,bauleras);

        return DashBoardResponse.UsuarioConMayorDeuda.builder()
                .nombre(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .deuda(usuario.getDeudaAcumulada())
                .cantidadBauleras(cantidadBauleras)
                .build();
    }

    private long contarBaulerasDeUsuario(Usuario usuario, List<Baulera> bauleras){
        return bauleras.stream()
                .filter(b-> b.getUsuarioAsignado() != null &&
                        b.getUsuarioAsignado().getIdUsuario().equals(usuario.getIdUsuario()))
                .count();
    }

    private int contarNuevosUsuariosEsteMes(List<Usuario> usuarios){
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        return (int) usuarios.stream()
                .filter(u -> u.getFechaCreacion() != null)
                .filter(u->{
                    LocalDate fechaCreacion = u.getFechaCreacion().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return !fechaCreacion.isBefore(inicioMes) && !fechaCreacion.isAfter(finMes);
                })
                .count();
    }

    private String obtenerNombreMes(int numeroMes) {
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return meses[numeroMes - 1];
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @CacheEvict(value = "dashboard", allEntries = true)
    public void limpiarTodoCacheDashboard(){
        log.info("Todo el cache del dashboard limpiado");
    }

    /*=================METODOS PARA "GRAFICOS"==========================*/
    private List<DashBoardResponse.DatoGrafico> calcularOcupacionPorMes(List<Baulera> bauleras){
        List<Baulera> baulerasOcupadas= bauleras.stream()
                .filter(b->b.getEstadoBaulera() == ESTADO_BAULERA.ocupada)
                .collect(Collectors.toList());

        Map<String,Long> ocupacionPorMes = new HashMap<>();
        for (int i = 11; i>=0; i--){
            LocalDate mes= LocalDate.now().minusMonths(i);
            String clave= obtenerNombreMes(mes.getMonthValue()) + " " + mes.getYear();
            ocupacionPorMes.put(clave, 0L);
        }

        for(Baulera b: baulerasOcupadas){
            Date fechaAsignacion = b.getFechaAsignacion();
            LocalDate fecha = fechaAsignacion.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String clave = obtenerNombreMes(fecha.getMonthValue()) +" "+ fecha.getYear();

            LocalDate doceMesesAtras = LocalDate.now().minusMonths(12);
            if(!fecha.isBefore(doceMesesAtras)){
                ocupacionPorMes.put(clave, ocupacionPorMes.getOrDefault(clave, 0L) + 1);
            }
        }

        List<DashBoardResponse.DatoGrafico> datosGrafico = new ArrayList<>();
        String[] colores = {
                "#4A6FA5", "#166DD0", "#38A169", "#D69E2E", "#ED8936",
                "#E53E3E", "#9F7AEA", "#ED64A6", "#4299E1", "#48BB78",
                "#ECC94B", "#ED8936"
        };

        int i = 0;
        for(Map.Entry<String,Long> entry: ocupacionPorMes.entrySet()){
            if(entry.getValue() > 0 || i>=6){
                datosGrafico.add(DashBoardResponse.DatoGrafico.builder()
                        .etiqueta(entry.getKey())
                        .valor(BigDecimal.valueOf(entry.getValue()))
                        .color(colores[i % colores.length])
                        .build()
                );
                i++;
            }
        }
        return datosGrafico;
    }

    private List<DashBoardResponse.DatoGrafico> calcularDistribucionPorTipo(List<Baulera> bauleras){
        Map<String,Long> distribucion = bauleras.stream()
                .collect(Collectors.groupingBy(
                        b->b.getTipoBaulera().getTipoBauleraNombre(),
                        Collectors.counting()
                ));

        long total = bauleras.size();
        List<DashBoardResponse.DatoGrafico> datosGrafico = new ArrayList<>();
        String[] colores = {"#4A6FA5", "#166DD0", "#38A169", "#D69E2E", "#ED8936", "#E53E3E", "#9F7AEA"};

        int i = 0;
        for(Map.Entry<String,Long> entry: distribucion.entrySet()){
            BigDecimal porcentaje = total > 0
                    ? new BigDecimal(entry.getValue())
                        .divide(new BigDecimal(total),4,RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            String etiqueta = String.format("%s (%.1f%%)", entry.getKey(), porcentaje.doubleValue());
            datosGrafico.add(DashBoardResponse.DatoGrafico.builder()
                    .etiqueta(etiqueta)
                    .valor(BigDecimal.valueOf(entry.getValue()))
                    .color(colores[i % colores.length])
                    .build()
            );
            i++;
        }
        return datosGrafico;
    }

    private List<DashBoardResponse.DatoGrafico> calcularIngresosPorTipo(List<Baulera> bauleras){
        List<Baulera> baulerasOcupadas = bauleras.stream()
                .filter(b-> b.getEstadoBaulera() ==ESTADO_BAULERA.ocupada)
                .collect(Collectors.toList());

        Map<String,BigDecimal> ingresosPorTipo = baulerasOcupadas.stream()
                .collect(Collectors.groupingBy(
                        b->b.getTipoBaulera().getTipoBauleraNombre(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                b->BigDecimal.valueOf(b.getTipoBaulera().getPrecioMensual()),
                                BigDecimal::add
                        )
                ));

        List<Map.Entry<String, BigDecimal>> entriesOrdenados = new ArrayList<>(ingresosPorTipo.entrySet());
        entriesOrdenados.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<DashBoardResponse.DatoGrafico> datos = new ArrayList<>();
        String[] colores = {"#4A6FA5", "#166DD0", "#38A169", "#D69E2E", "#ED8936", "#E53E3E"};

        for (int i = 0; i<entriesOrdenados.size(); i++){
            Map.Entry<String,BigDecimal> entry = entriesOrdenados.get(i);

            datos.add(DashBoardResponse.DatoGrafico.builder()
                    .etiqueta(entry.getKey())
                    .valor(entry.getValue())
                    .color(colores[i % colores.length])
                    .build()
            );
        }
        return datos;
    }
}

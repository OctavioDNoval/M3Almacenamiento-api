package com.example.m3almacenamiento.servicios.scheduler;

import com.example.m3almacenamiento.modelo.DTO.InfoDeudaEmail;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import com.example.m3almacenamiento.servicios.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagosScheduler {
    private final UsuarioRepositorio usuarioRepositorio;
    private final BauleraRepositorio bauleraRepositorio;
    private final EmailService emailService;

    /*
     * Se ejecuta todos los meses
     * a las 6 am y suma a los usuarios
     * la deuda del total de sus bauleras
     * */
    @Scheduled(cron = "0 0 6 1 * *")
    @Transactional
    public void actualizarDeudas(){
        log.info("==== Inciando actualizacion de deudas mensual ====");

        List<Baulera> baulerasActivas = bauleraRepositorio.findAllOcupadas();
        log.info("Total de bauleras activas: {}",baulerasActivas.size());
        Map<Usuario, InfoDeudaEmail> infoPorUsuario = new HashMap<>();

        if(baulerasActivas.isEmpty()){
            log.info("No hay bauleras ocupadas");
            return;
        }

        for(Baulera b :baulerasActivas){
            log.info("Revisando bauleras: {}", b.getNroBaulera());
            Usuario u = b.getUsuarioAsignado();

            if(u == null){
                log.warn("Usuario no encontrado para baulera {}", b.getNroBaulera());
                continue;
            }

            Double precioBaulera = b.getTipoBaulera().getPrecioMensual();
            BigDecimal precioMensual = BigDecimal.valueOf(precioBaulera);

            InfoDeudaEmail info = infoPorUsuario.get(u);
            if(info == null){
                info = new InfoDeudaEmail(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new ArrayList<>(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );
                infoPorUsuario.put(u, info);
            }

            info.setMontoMensual(info.getMontoMensual().add(precioMensual));
            info.setTotalCalculado(info.getTotalCalculado().add(precioMensual));
            info.getNumerosBauleras().add(b.getNroBaulera());

            log.info("Sumando ${} por baulera {} al usuario {}",
                    precioMensual, b.getNroBaulera(), u.getNombreCompleto());

        }

        int usuarioActualizados = 0;
        for (Map.Entry<Usuario, InfoDeudaEmail> entry : infoPorUsuario.entrySet()) {
            Usuario usuario = entry.getKey();
            InfoDeudaEmail info = entry.getValue();

            try{
                BigDecimal deudaActual = usuario.getDeudaAcumulada() != null
                        ? usuario.getDeudaAcumulada()
                        : BigDecimal.ZERO;
                info.setDeudaAnterior(deudaActual);

                BigDecimal nuevaDeuda = deudaActual.add(info.getMontoMensual());
                info.setNuevaDeudaTotaL(nuevaDeuda);

                usuario.setDeudaAcumulada(nuevaDeuda);
                usuarioRepositorio.save(usuario);

                log.info("Usuario: {}, deuda anterior {} -> {} deuda nueva",
                        usuario.getNombreCompleto(), deudaActual, nuevaDeuda);

                usuarioActualizados++;
                emailService.enviarNotificacionDeuda(usuario, info);
            }catch(Exception e){
                log.error("Error actualizando deuda de {}: {}", usuario.getNombreCompleto(), e.getMessage());
            }
        }
        log.info("==== Actualización de deudas completada ====");
        log.info("Usuarios actualizados: {}", usuarioActualizados);

        BigDecimal totalGenerado = infoPorUsuario.values().stream()
                .map(InfoDeudaEmail::getMontoMensual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total de deuda generada este mes: {}", totalGenerado);
    }

    // Cambia temporalmente el método para aislar el problema
    @Scheduled(cron = "0 0 6 1 * *")
    public void actualizarDeudasPrueba(){
        log.info("==== PRUEBA DIAGNÓSTICO ====");

        List<Baulera> baulerasActivas = bauleraRepositorio.findAllOcupadas();
        log.info("Bauleras encontradas: {}", baulerasActivas.size());

        for(Baulera b : baulerasActivas) {
            log.info("--- Baulera ID: {}, NRO: {}", b.getIdBaulera(), b.getNroBaulera());
            log.info("Usuario asignado: {}", b.getUsuarioAsignado() != null ? b.getUsuarioAsignado().getNombreCompleto() : "NULL");
            log.info("TipoBaulera: {}", b.getTipoBaulera() != null ? b.getTipoBaulera().getTipoBauleraNombre() : "NULL");
            if(b.getTipoBaulera() != null) {
                log.info("Precio mensual: {}", b.getTipoBaulera().getPrecioMensual());
            }
            log.info("---");
        }

        log.info("==== FIN PRUEBA ====");
    }


    private boolean yaCobradoEsteMes(Usuario u, Calendar hoy){
        if(u.getUltimaActualizacionDeuda() == null){
            return false;
        }
        Calendar ultimoCobro = Calendar.getInstance();
        ultimoCobro.setTime(u.getUltimaActualizacionDeuda());

        return ultimoCobro.get(Calendar.YEAR) ==  hoy.get(Calendar.YEAR) &&
                ultimoCobro.get(Calendar.MONTH) ==  hoy.get(Calendar.MONTH);
    }
}

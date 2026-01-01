package com.example.m3almacenamiento.servicios.scheduler;

import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagosScheduler {
    private final UsuarioRepositorio usuarioRepositorio;
    private final BauleraRepositorio bauleraRepositorio;

    /*
     * Se ejecuta todos los dias a la 1Am
     * Vamos a mostrar en los logs para que quede marcado
     * Cuando se inicia el procedimiento
     * */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void actualizarDeudas(){
        log.info("==== Inciando actualizacion de deudas diarias ====");

        //Buscamos que dia del mes es hoy
        Calendar calendar = Calendar.getInstance();
        int diaMes = calendar.get(Calendar.DAY_OF_MONTH);

        //Buscamos las bauleras que vencen en la fecha de hoy
        List<Baulera> baulerasAVencer = bauleraRepositorio.findByDiaVencimientoPago(diaMes);
        log.info("Bauleras a vencer el dia {} : {}", diaMes, baulerasAVencer);

        for(Baulera b : baulerasAVencer){
            if(b.getUsuarioAsignado() == null){
                continue;
            }

            try{
                cobrarBaulera(b,calendar);
            }catch(Exception e){
                log.error("❌ Error cobrando baulera: {}, {}", b.getNroBaulera(), e.getMessage());
            }
        }
        log.info("==== Actaulizacion de deudas Completada ====");
    }

    private void cobrarBaulera(Baulera baulera, Calendar hoy){
        Usuario usuario = baulera.getUsuarioAsignado();

        if(yaCobradoEsteMes(usuario, hoy)){
            return;
        }

        BigDecimal precioMensual = BigDecimal.valueOf(
                baulera.getTipoBaulera().getPrecioMensual());

        BigDecimal deudaActual = usuario.getDeudaAcumulada() != null
                ? usuario.getDeudaAcumulada()
                : BigDecimal.ZERO;

        BigDecimal nuevaDeuda = deudaActual.add(precioMensual);
        usuario.setDeudaAcumulada(nuevaDeuda);
        usuario.setUltimaActualizacionDeuda(hoy.getTime());
        usuarioRepositorio.save(usuario);

        log.info("Deuda actualizada correctamente para \n" +
                "Usuario: {}\n" +
                "Baulera: {}\n" +
                "Monto Viejo: {}\n" +
                "Monto sumado: {}\n" +
                "Monto nuevo: {}\n",
                usuario.getNombreCompleto(), baulera.getNroBaulera(),deudaActual, precioMensual, nuevaDeuda);
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

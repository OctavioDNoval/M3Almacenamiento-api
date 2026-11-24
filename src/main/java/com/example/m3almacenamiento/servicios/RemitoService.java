package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.excepciones.IllegalInputValues;
import com.example.m3almacenamiento.excepciones.ResourceNotFoundException;
import com.example.m3almacenamiento.modelo.DTO.request.RemitoManualRequest;
import com.example.m3almacenamiento.modelo.DTO.InfoDeudaEmail;


import com.example.m3almacenamiento.modelo.DTO.mapeo.RemitoMapper;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.RemitoResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Remito;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.RemitoRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RemitoService {
    private final RemitoRepositorio remitoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final EmailService emailService;
    private final RemitoMapper remitoMapper;
    private final BauleraRepositorio bauleraRepositorio;

    public PaginacionResponse<RemitoResponse> obtenerTodosPaginados(Integer pagina, Integer tamanio, String sortBy, String direction){
        Sort sort =  buildSort(sortBy, direction);
        Pageable pageable =  PageRequest.of(pagina - 1, tamanio, sort );
        Page<Remito> page = remitoRepositorio.findAll(pageable);
        return getRemitoResponsePaginacionResponse(pagina,tamanio,page);
    }

    public PaginacionResponse<RemitoResponse> obtenerPaginadoConFiltro(Integer pagina, Integer tamanio, String sortBy, String filter, String direction){
        Sort sort = buildSort(sortBy, direction);
        Pageable pageable = PageRequest.of(pagina - 1, tamanio, sort );
        Page<Remito> page = remitoRepositorio.findBySearch(filter, pageable);
        return getRemitoResponsePaginacionResponse(pagina,tamanio,page);
    }

    private PaginacionResponse<RemitoResponse> getRemitoResponsePaginacionResponse(Integer pagina, Integer tamanio, Page<Remito> paginaRemito) {
        List<RemitoResponse> contenido = paginaRemito.getContent()
                .stream()
                .map(remitoMapper::toResponse)
                .collect(Collectors.toList());

        return PaginacionResponse.<RemitoResponse>builder()
                .contenido(contenido)
                .pagina(pagina)
                .tamanio(tamanio)
                .totalElementos(paginaRemito.getTotalElements())
                .totalPaginas(paginaRemito.getTotalPages())
                .esUltima(paginaRemito.isLast())
                .esPrimera(paginaRemito.isFirst())
                .build();
    }

    private Sort buildSort(String sortBy, String direction){
        Map<String,String> mapeoCampos = Map.of(
                "idRemito", "idRemito",
                "periodo", "periodo",
                "nombreUsuario", "usuario.nombreCompleto"
        );

        String campoReal = mapeoCampos.getOrDefault(sortBy, "idUsuario");
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir,campoReal);
    }

    public Remito generarRemitoPorUsuario(UUID idUsuario, Integer mes){
        if (mes == null || mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mes debe ser un valor entre 1 y 12");
        }
        Usuario u = usuarioRepositorio.findByIdPublico(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Remito r= new Remito();
        LocalDate fecha = LocalDate.now();
        List<Baulera> bauleras = u.getBauleras();
        BigDecimal total = BigDecimal.ZERO;
        String stringBauleras="";

        r.setUsuario(u);
        r.setDeudaAnterior(BigDecimal.ZERO);
        r.setFechaEmision(fecha);
        for(int i = 0; i < bauleras.size(); i++){
            stringBauleras = stringBauleras.concat(bauleras.get(i).getNroBaulera());
            if(i != bauleras.size() - 1){
                stringBauleras = stringBauleras.concat(",");
            }
            total = total.add(BigDecimal.valueOf(bauleras.get(i).getTipoBaulera().getPrecioMensual()));
        }
        r.setImporteTotal(total);
        r.setBaulerasString(stringBauleras);
        String nombreMes = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es","ES"));
        r.setPeriodo(nombreMes.toUpperCase() + " " + String.valueOf(fecha.getYear()));
        return r;
    }

    public void generarRemito(Usuario usuario, InfoDeudaEmail infoDeudaEmail){
        Remito remito = new Remito();
        remito.setUsuario(usuario);
        remito.setPeriodo(emailService.getNombreMesActual());
        remito.setFechaEmision(LocalDate.now());
        remito.setImporteTotal(infoDeudaEmail.getTotalCalculado());
        remito.setDeudaAnterior(infoDeudaEmail.getDeudaAnterior());

        String bauleras = "";
        List<String> listaBauleras = infoDeudaEmail.getNumerosBauleras();
        for(int i = 0; i < listaBauleras.size(); i++){
            bauleras = bauleras.concat(listaBauleras.get(i));  
            if(i != listaBauleras.size() - 1){
                bauleras = bauleras.concat(",");  
            }
        }

        remito.setBaulerasString(bauleras);
        remitoRepositorio.save(remito);
    }

    /*
     * Remito armado a mano por el admin. No modifica la deuda del usuario:
     * el remito es solo el comprobante. Se persiste únicamente si request.guardar es true.
     */
    public Remito crearRemitoManual(RemitoManualRequest request){
        if (request == null || request.getIdUsuario() == null) {
            throw new IllegalInputValues("Elegí el cliente del remito");
        }
        YearMonth periodo = parsearPeriodo(request.getPeriodo());

        List<String> bauleras = request.getBauleras() == null ? List.of() : request.getBauleras().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(nro -> !nro.isEmpty())
                .distinct()
                .toList();
        if (bauleras.isEmpty()) {
            throw new IllegalInputValues("El remito tiene que incluir al menos una baulera");
        }
        for (String nro : bauleras) {
            if (bauleraRepositorio.findByNroBaulera(nro).isEmpty()) {
                throw new IllegalInputValues("La baulera " + nro + " no existe");
            }
        }

        BigDecimal deudaAnterior = request.getDeudaAnterior() == null ? BigDecimal.ZERO : request.getDeudaAnterior();
        BigDecimal montoMes = request.getMontoMes();
        if (montoMes == null) {
            throw new IllegalInputValues("Ingresá el monto del mes");
        }
        // deudaAnterior puede ser negativa: es saldo a favor del cliente (pago adelantado)
        if (montoMes.signum() < 0) {
            throw new IllegalInputValues("El monto del mes no puede ser negativo");
        }

        Usuario usuario = usuarioRepositorio.findByIdPublico(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Remito remito = new Remito();
        remito.setUsuario(usuario);
        remito.setPeriodo(nombrePeriodo(periodo));
        remito.setFechaEmision(LocalDate.now());
        remito.setImporteTotal(montoMes);
        remito.setDeudaAnterior(deudaAnterior);
        remito.setBaulerasString(String.join(",", bauleras));

        return request.isGuardar() ? remitoRepositorio.save(remito) : remito;
    }

    private YearMonth parsearPeriodo(String periodo){
        if (periodo == null || periodo.isBlank()) {
            throw new IllegalInputValues("Elegí el mes del remito");
        }
        try {
            return YearMonth.parse(periodo.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalInputValues("El mes del remito no es válido");
        }
    }

    // Mismo formato que los remitos del scheduler: "Noviembre 2026"
    static String nombrePeriodo(YearMonth periodo){
        String mes = periodo.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        return mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + periodo.getYear();
    }
}

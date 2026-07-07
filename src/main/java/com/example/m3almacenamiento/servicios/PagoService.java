package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.PagoMapper;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PagoResponse;
import com.example.m3almacenamiento.modelo.entidad.Pago;
import com.example.m3almacenamiento.repositorios.PagoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PagoService {
    private final PagoRepositorio repositorio;
    private final PagoMapper pagoMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public PaginacionResponse<PagoResponse> obtenerTodosPaginados(Integer pagina, Integer tamanio, String sortBy, String direction){
        Sort sort = buildSort(sortBy, direction);
        Pageable pageable = PageRequest.of(pagina - 1, tamanio, sort);
        Page<Pago> page = repositorio.findAll(pageable);
        return getPagoResponsePaginacionResponse(pagina, tamanio, page);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginacionResponse<PagoResponse> obtenerPaginadoConFiltro(Integer pagina, Integer tamanio, String sortBy, String filter, String direction){
        Sort sort = buildSort(sortBy, direction);
        Pageable pageable = PageRequest.of(pagina - 1, tamanio, sort);
        Page<Pago> page = repositorio.findBySearch(filter, pageable);
        return getPagoResponsePaginacionResponse(pagina, tamanio, page);
    }

    private PaginacionResponse<PagoResponse> getPagoResponsePaginacionResponse(Integer pagina, Integer tamanio, Page<Pago> paginaPago) {
        List<PagoResponse> contenido = paginaPago.getContent()
                .stream()
                .map(pagoMapper::toResponse)
                .collect(Collectors.toList());

        return PaginacionResponse.<PagoResponse>builder()
                .contenido(contenido)
                .pagina(pagina)
                .tamanio(tamanio)
                .totalElementos(paginaPago.getTotalElements())
                .totalPaginas(paginaPago.getTotalPages())
                .esUltima(paginaPago.isLast())
                .esPrimera(paginaPago.isFirst())
                .build();
    }

    private Sort buildSort(String sortBy, String direction) {
        Map<String, String> mapeoCampos = Map.of(
                "idPago", "idPago",
                "montoPagado", "montoPagado",
                "fechaPago", "fechaPago",
                "nombreUsuario", "usuario.nombreCompleto"
        );

        String campoReal = mapeoCampos.getOrDefault(sortBy, "idPago");
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, campoReal);
    }

    public List<PagoResponse> obtenerPorUsuario(UUID idPublicoUsuario){
        return repositorio.getPagoByUsuario_IdPublico(idPublicoUsuario)
                .stream()
                .map(pagoMapper::toResponse)
                .collect(Collectors.toList());
    }
}


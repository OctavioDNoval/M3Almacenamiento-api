package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.mapeo.LogMapper;
import com.example.m3almacenamiento.modelo.DTO.response.LogResponse;
import com.example.m3almacenamiento.modelo.DTO.response.PaginacionResponse;
import com.example.m3almacenamiento.modelo.entidad.Log;
import com.example.m3almacenamiento.repositorios.LogRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepositorio logRepositorio;
    private final LogMapper logMapper;

    public PaginacionResponse<LogResponse> obtenerLogsPaginados(
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "15") Integer tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio, Sort.by("fecha").descending());
        Page<Log> paginaLog = logRepositorio.findAll(pageable);

        List<LogResponse> contenido = paginaLog.getContent()
                .stream()
                .map(logMapper::toResponse)
                .collect(Collectors.toList());

        return PaginacionResponse.<LogResponse>builder()
                .contenido(contenido)
                .pagina(pagina)
                .tamanio(tamanio)
                .totalElementos(paginaLog.getTotalElements())
                .totalPaginas(paginaLog.getTotalPages())
                .esUltima(paginaLog.isLast())
                .esPrimera(paginaLog.isFirst())
                .build();
    }

    public List<LogResponse> obtenerPorAccion(String accion) {
        return logRepositorio.findTop15ByAccionOrderByFechaDesc(accion)
                .stream()
                .map(logMapper::toResponse)
                .collect(Collectors.toList());
    }
}

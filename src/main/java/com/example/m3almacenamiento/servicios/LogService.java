package com.example.m3almacenamiento.servicios;

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

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepositorio logRepositorio;

    public PaginacionResponse<Log> obtenerLogsPaginados(
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "15") Integer tamanio) {
        Pageable pageable = PageRequest.of(pagina - 1, tamanio, Sort.by("fecha").descending());
        Page<Log> paginaLog = logRepositorio.findAll(pageable);

        List<Log> contenido = paginaLog.getContent();

        return PaginacionResponse.<Log>builder()
                .contenido(contenido)
                .pagina(pagina)
                .tamanio(tamanio)
                .totalElementos(paginaLog.getTotalElements())
                .totalPaginas(paginaLog.getTotalPages())
                .esUltima(paginaLog.isLast())
                .esPrimera(paginaLog.isFirst())
                .build();
    }
}

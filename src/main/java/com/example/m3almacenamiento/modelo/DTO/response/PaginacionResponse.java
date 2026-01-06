package com.example.m3almacenamiento.modelo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginacionResponse <T>{
    private List<T> contenido;
    private Integer pagina;
    private Integer tamanio;
    private Long totalElementos;
    private Integer totalPaginas;
    private Boolean esUltima;
    private Boolean esPrimera;
}

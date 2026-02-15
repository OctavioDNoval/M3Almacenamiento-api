package com.example.m3almacenamiento.modelo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogResponse {
    private Long idLog;
    private String usuario;
    private String accion;
    private String tablaAfectada;
    private String descripcion;
    private LocalDateTime fecha;
}

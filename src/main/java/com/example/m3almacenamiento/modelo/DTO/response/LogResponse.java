package com.example.m3almacenamiento.modelo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogResponse {
    private UUID idLog;
    private String usuario;
    private String accion;
    private String tablaAfectada;
    private String valoresAnteriores;
    private String valoresNuevos;
    private String descripcion;
    private LocalDateTime fecha;
}

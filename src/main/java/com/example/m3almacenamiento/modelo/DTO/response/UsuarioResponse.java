package com.example.m3almacenamiento.modelo.DTO.response;

import com.example.m3almacenamiento.modelo.enumerados.ESTADO_USUARIO;
import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioResponse {
    @JsonProperty("id")
    private Long idUsuario;

    private String dni;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private ROL rol;
    private ESTADO_USUARIO estado;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;
    private BigDecimal deudaAcumulada;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimaActualizacionDeuda;
    private List<Long> idBauleras;
    private List<String> nroBaulera;
}

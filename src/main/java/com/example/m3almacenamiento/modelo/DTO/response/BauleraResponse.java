package com.example.m3almacenamiento.modelo.DTO.response;

import com.example.m3almacenamiento.modelo.enumerados.ESTADO_BAULERA;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BauleraResponse {
    //Informacion de la baulera en si
    private Long idBaulera;
    private String nroBaulera;
    private ESTADO_BAULERA estadoBaulera;
    private String observaciones;

    //Informacion del tipo de baulera
    private Long idTipoBaulera;
    private String tipoBauleraNombre;
    private Double tipoBauleraPrecio;

    //Informacion del usuario, si es que la baulera tiene uno asignado
    private Long idUsuario;
    private String nombreUsuario;
    private String emailUsuario;
    @JsonFormat(shape =  JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date fechaAsignacion;

    @JsonProperty("isDisponible")
    public boolean isDisponible() {
        return ESTADO_BAULERA.disponible.equals(estadoBaulera);
    }

    @JsonProperty("isOcupada")
    public boolean isOcupada() {
        return ESTADO_BAULERA.ocupada.equals(estadoBaulera);
    }
}

package com.example.m3almacenamiento.modelo.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "remito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Remito {
    @Id
    @Column(name = "id_remito")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRemito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "periodo", length = 30, nullable = false)
    private String periodo;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "importe_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal importeTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_json", columnDefinition = "json")
    private String datosJson;


}

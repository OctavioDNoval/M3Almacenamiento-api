package com.example.m3almacenamiento.modelo.entidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.UUID;

@Table(name = "remito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Remito {
    @Id
    @Column(name = "id_remito")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRemito;

    @Column(name = "id_publico", unique = true, nullable = false, columnDefinition = "CHAR(36)", updatable = false)
    @JdbcTypeCode(Types.CHAR)
    @Builder.Default
    private UUID idPublico = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "periodo", length = 30, nullable = false)
    private String periodo;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "importe_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal importeTotal;

    @Column(name = "bauleras_string")
    private String baulerasString;

    @Column(name = "deuda_anterior")
    private BigDecimal deudaAnterior;


}

package com.example.m3almacenamiento.modelo.entidad;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pago")
@Builder
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @Column(nullable = false, unique = true, name = "id_publico")
    @JdbcTypeCode(Types.CHAR)
    @Builder.Default
    private UUID idPublico = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario usuario;

    @Min(value = 0)
    @NotNull
    private BigDecimal montoPagado;

    @Builder.Default
    private LocalDate fechaPago=LocalDate.now();
}

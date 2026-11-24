package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.excepciones.IllegalInputValues;
import com.example.m3almacenamiento.excepciones.ResourceNotFoundException;
import com.example.m3almacenamiento.modelo.DTO.request.RemitoManualRequest;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Remito;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.BauleraRepositorio;
import com.example.m3almacenamiento.repositorios.RemitoRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemitoServiceManualTest {

    @Mock RemitoRepositorio remitoRepositorio;
    @Mock UsuarioRepositorio usuarioRepositorio;
    @Mock BauleraRepositorio bauleraRepositorio;
    @Mock EmailService emailService;
    @Mock com.example.m3almacenamiento.modelo.DTO.mapeo.RemitoMapper remitoMapper;

    @InjectMocks RemitoService service;

    private final UUID idUsuario = UUID.randomUUID();
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().idUsuario(1L).nombreCompleto("Ana Pérez")
                .deudaAcumulada(new BigDecimal("50000")).build();
        lenient().when(usuarioRepositorio.findByIdPublico(idUsuario)).thenReturn(Optional.of(usuario));
        lenient().when(bauleraRepositorio.findByNroBaulera(anyString())).thenReturn(Optional.of(new Baulera()));
    }

    private RemitoManualRequest.RemitoManualRequestBuilder request() {
        return RemitoManualRequest.builder()
                .idUsuario(idUsuario)
                .periodo("2026-03")
                .bauleras(List.of("12", " 14 ", "12"))
                .deudaAnterior(new BigDecimal("50000"))
                .montoMes(new BigDecimal("45000"));
    }

    @Test
    void armaElRemitoConLosDatosIngresadosSinGuardarloPorDefecto() {
        Remito r = service.crearRemitoManual(request().build());

        assertThat(r.getUsuario()).isSameAs(usuario);
        assertThat(r.getPeriodo()).isEqualTo("Marzo 2026");
        assertThat(r.getBaulerasString()).isEqualTo("12,14");
        assertThat(r.getImporteTotal()).isEqualByComparingTo("45000");
        assertThat(r.getDeudaAnterior()).isEqualByComparingTo("50000");
        assertThat(r.getFechaEmision()).isEqualTo(LocalDate.now());
        verify(remitoRepositorio, never()).save(any());
        // El remito es solo el comprobante: no toca la deuda del cliente
        assertThat(usuario.getDeudaAcumulada()).isEqualByComparingTo("50000");
    }

    @Test
    void loGuardaSoloSiSeLoPiden() {
        when(remitoRepositorio.save(any(Remito.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crearRemitoManual(request().guardar(true).build());

        verify(remitoRepositorio).save(any(Remito.class));
    }

    @Test
    void deudaAnteriorVaciaCuentaComoCero() {
        Remito r = service.crearRemitoManual(request().deudaAnterior(null).build());
        assertThat(r.getDeudaAnterior()).isEqualByComparingTo("0");
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThatThrownBy(() -> service.crearRemitoManual(request().idUsuario(null).build()))
                .isInstanceOf(IllegalInputValues.class);
        assertThatThrownBy(() -> service.crearRemitoManual(request().periodo("marzo").build()))
                .isInstanceOf(IllegalInputValues.class);
        assertThatThrownBy(() -> service.crearRemitoManual(request().bauleras(List.of(" ")).build()))
                .isInstanceOf(IllegalInputValues.class);
        assertThatThrownBy(() -> service.crearRemitoManual(request().montoMes(null).build()))
                .isInstanceOf(IllegalInputValues.class);
        assertThatThrownBy(() -> service.crearRemitoManual(request().montoMes(new BigDecimal("-1")).build()))
                .isInstanceOf(IllegalInputValues.class);
    }

    @Test
    void rechazaBaulerasInexistentesYUsuarioInexistente() {
        when(bauleraRepositorio.findByNroBaulera("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crearRemitoManual(request().bauleras(List.of("99")).build()))
                .isInstanceOf(IllegalInputValues.class)
                .hasMessageContaining("99");

        UUID otro = UUID.randomUUID();
        when(usuarioRepositorio.findByIdPublico(otro)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crearRemitoManual(request().idUsuario(otro).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void nombreDelPeriodoComoLosDelScheduler() {
        assertThat(RemitoService.nombrePeriodo(YearMonth.of(2026, 11))).isEqualTo("Noviembre 2026");
    }
}

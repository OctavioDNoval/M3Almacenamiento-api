package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.excepciones.IllegalInputValues;
import com.example.m3almacenamiento.modelo.DTO.mapeo.UsuarioMapper;
import com.example.m3almacenamiento.modelo.entidad.Pago;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.repositorios.PagoRepositorio;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceReducirDeudaTest {

    @Mock UsuarioRepositorio usuarioRepositorio;
    @Mock UsuarioMapper usuarioMapper;
    @Mock PagoRepositorio pagoRepositorio;
    @InjectMocks UsuarioService service;

    private final UUID id = UUID.randomUUID();

    private Usuario conDeuda(String deuda) {
        Usuario u = Usuario.builder().nombreCompleto("Ana").deudaAcumulada(new BigDecimal(deuda)).build();
        when(usuarioRepositorio.findByIdPublico(id)).thenReturn(Optional.of(u));
        when(usuarioRepositorio.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        return u;
    }

    @Test
    void pagoMayorALaDeudaDejaSaldoAFavor() {
        Usuario u = conDeuda("45000");

        service.reducirDeuda(id, 60000);

        assertThat(u.getDeudaAcumulada()).isEqualByComparingTo("-15000");
        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepositorio).save(pago.capture());
        assertThat(pago.getValue().getMontoPagado()).isEqualByComparingTo("60000");
    }

    @Test
    void clienteAlDiaPuedePagarPorAdelantado() {
        Usuario u = conDeuda("0");

        service.reducirDeuda(id, 25000);

        assertThat(u.getDeudaAcumulada()).isEqualByComparingTo("-25000");
    }

    @Test
    void montoCeroONegativoSigueRechazado() {
        assertThatThrownBy(() -> service.reducirDeuda(id, 0)).isInstanceOf(IllegalInputValues.class);
        verify(pagoRepositorio, never()).save(any());
    }
}

package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.modelo.DTO.response.DashBoardResponse;
import com.example.m3almacenamiento.modelo.entidad.Baulera;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashBoardServiceTopDeudoresTest {

    // Los repositorios y el mapper no se usan en el cálculo
    private final DashBoardService service = new DashBoardService(null, null, null);

    private static long ids = 0;

    private Usuario usuario(String nombre, String deuda) {
        return Usuario.builder()
                .idUsuario(++ids)
                .nombreCompleto(nombre)
                .email(nombre.toLowerCase() + "@test.com")
                .deudaAcumulada(deuda == null ? null : new BigDecimal(deuda))
                .build();
    }

    private Baulera baulera(Usuario u) {
        Baulera b = new Baulera();
        b.setUsuarioAsignado(u);
        return b;
    }

    @Test
    void ordenaDeMayorAMenorYRespetaElLimite() {
        List<Usuario> usuarios = List.of(
                usuario("Ana", "1000"), usuario("Beto", "5000"), usuario("Caro", "3000"),
                usuario("Dani", "4000"), usuario("Eli", "2000"), usuario("Fede", "6000"));

        List<DashBoardResponse.UsuarioConMayorDeuda> top =
                service.calcularTopDeudores(usuarios, List.of(), 5);

        assertThat(top).extracting(DashBoardResponse.UsuarioConMayorDeuda::getNombre)
                .containsExactly("Fede", "Beto", "Dani", "Caro", "Eli");
    }

    @Test
    void excluyeClientesSinDeudaOConDeudaNula() {
        List<Usuario> usuarios = List.of(
                usuario("Ana", "0"), usuario("Beto", null), usuario("Caro", "1500.50"));

        List<DashBoardResponse.UsuarioConMayorDeuda> top =
                service.calcularTopDeudores(usuarios, List.of(), 5);

        assertThat(top).hasSize(1);
        assertThat(top.get(0).getNombre()).isEqualTo("Caro");
        assertThat(top.get(0).getDeuda()).isEqualByComparingTo("1500.50");
    }

    @Test
    void cuentaLasBaulerasDeCadaDeudor() {
        Usuario ana = usuario("Ana", "9000");
        Usuario beto = usuario("Beto", "100");
        List<Baulera> bauleras = new ArrayList<>(List.of(baulera(ana), baulera(ana), baulera(beto)));
        bauleras.add(baulera(null));

        List<DashBoardResponse.UsuarioConMayorDeuda> top =
                service.calcularTopDeudores(List.of(ana, beto), bauleras, 5);

        assertThat(top).extracting(DashBoardResponse.UsuarioConMayorDeuda::getCantidadBauleras)
                .containsExactly(2L, 1L);
    }

    @Test
    void sinDeudoresDevuelveListaVacia() {
        assertThat(service.calcularTopDeudores(List.of(usuario("Ana", "0")), List.of(), 5)).isEmpty();
    }

    @Test
    void elSaldoAFavorNoRestaDeLaDeudaTotal() {
        List<Usuario> usuarios = List.of(
                usuario("Ana", "100000"), usuario("Beto", "-30000"), usuario("Caro", null));

        assertThat(service.calcularDeudaTotal(usuarios)).isEqualByComparingTo("100000");
    }

    @Test
    void conTodosAlDiaYUnSaldoAFavorLaDeudaTotalEsCero() {
        List<Usuario> usuarios = List.of(usuario("Ana", "0"), usuario("Beto", "-25000"));

        assertThat(service.calcularDeudaTotal(usuarios)).isEqualByComparingTo("0");
        assertThat(service.calcularTopDeudores(usuarios, List.of(), 5)).isEmpty();
    }
}

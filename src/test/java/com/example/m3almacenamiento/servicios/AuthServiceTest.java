package com.example.m3almacenamiento.servicios;

import com.example.m3almacenamiento.configuracion.JwtService;
import com.example.m3almacenamiento.modelo.DTO.auth.LoginRequest;
import com.example.m3almacenamiento.modelo.DTO.mapeo.UsuarioMapper;
import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepositorio usuarioRepositorio;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UsuarioMapper usuarioMapper;

    @InjectMocks private AuthService authService;

    @Test
    void clientsCannotSignInEvenWithTheRightPassword() {
        Usuario cliente = new Usuario();
        cliente.setEmail("cliente@test.com");
        cliente.setPasswordHash("hashed");
        cliente.setRol(ROL.USER);

        LoginRequest request = new LoginRequest();
        request.setEmail("cliente@test.com");
        request.setPassword("ok");

        when(usuarioRepositorio.findByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void semiadminCanSignIn() {
        Usuario usuario = new Usuario();
        usuario.setEmail("semi@test.com");
        usuario.setPasswordHash("hashed");
        usuario.setRol(ROL.SEMIADMIN);

        LoginRequest request = new LoginRequest();
        request.setEmail("semi@test.com");
        request.setPassword("ok");

        when(usuarioRepositorio.findByEmail("semi@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ok", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("token");

        assertThat(authService.login(request).getToken()).isEqualTo("token");
    }

    @Test
    void loginFailsWithWrongPassword() {
        Usuario usuario = new Usuario();
        usuario.setEmail("a@test.com");
        usuario.setPasswordHash("hashed");
        usuario.setRol(ROL.ADMIN);

        LoginRequest request = new LoginRequest();
        request.setEmail("a@test.com");
        request.setPassword("wrong");

        when(usuarioRepositorio.findByEmail("a@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginFailsWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@test.com");
        request.setPassword("x");
        when(usuarioRepositorio.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void loginReturnsTokenWithCorrectPassword() {
        Usuario usuario = new Usuario();
        usuario.setEmail("a@test.com");
        usuario.setPasswordHash("hashed");
        usuario.setRol(ROL.ADMIN);

        LoginRequest request = new LoginRequest();
        request.setEmail("a@test.com");
        request.setPassword("ok");

        when(usuarioRepositorio.findByEmail("a@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("ok", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("token");

        assertThat(authService.login(request).getToken()).isEqualTo("token");
    }
}

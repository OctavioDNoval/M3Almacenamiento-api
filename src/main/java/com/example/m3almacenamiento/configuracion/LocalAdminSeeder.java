package com.example.m3almacenamiento.configuracion;

import com.example.m3almacenamiento.modelo.entidad.Usuario;
import com.example.m3almacenamiento.modelo.enumerados.ROL;
import com.example.m3almacenamiento.repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates an ADMIN user on startup when running with the "local" profile
 * (docker-compose stack), so an empty database can be logged into.
 */
@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class LocalAdminSeeder implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.admin.dni:00000000}")
    private String adminDni;

    @Override
    public void run(String... args) {
        if (Boolean.TRUE.equals(usuarioRepositorio.existsByEmail(adminEmail))) {
            return;
        }
        usuarioRepositorio.save(Usuario.builder()
                .dni(adminDni)
                .nombreCompleto("Admin Local")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .rol(ROL.ADMIN)
                .build());
        log.info("Local admin user created: {}", adminEmail);
    }
}

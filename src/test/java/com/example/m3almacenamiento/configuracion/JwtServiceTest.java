package com.example.m3almacenamiento.configuracion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-with-at-least-32-bytes!!".getBytes());

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "key", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 60_000L);
    }

    private UserDetails user(String email, String rol) {
        return User.withUsername(email).password("x").roles(rol).build();
    }

    @Test
    void generatedTokenContainsEmailAndRole() {
        String token = jwtService.generateToken(user("admin@test.com", "ADMIN"));

        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@test.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void tokenIsValidOnlyForItsOwner() {
        String token = jwtService.generateToken(user("a@test.com", "USER"));

        assertThat(jwtService.isTokenValid(token, user("a@test.com", "USER"))).isTrue();
        assertThat(jwtService.isTokenValid(token, user("b@test.com", "USER"))).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1_000L);
        String token = jwtService.generateToken(user("a@test.com", "USER"));

        assertThat(jwtService.isTokenExpired(token)).isTrue();
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void tamperedOrGarbageTokenIsInvalid() {
        String token = jwtService.generateToken(user("a@test.com", "USER"));
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void tokenSignedWithAnotherKeyIsInvalid() {
        JwtService other = new JwtService();
        ReflectionTestUtils.setField(other, "key", Base64.getEncoder()
                .encodeToString("another-secret-key-with-32-bytes-min!!".getBytes()));
        ReflectionTestUtils.setField(other, "expiration", 60_000L);

        String foreignToken = other.generateToken(user("a@test.com", "ADMIN"));

        assertThat(jwtService.isTokenValid(foreignToken)).isFalse();
    }
}

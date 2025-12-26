package com.example.m3almacenamiento.configuracion;

import com.example.m3almacenamiento.modelo.enumerados.ROL;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional

public class JwtService {
    @Value("${jwt.secret}")
    private String key;

    @Value("${jwt.expiration}")
    private long expiration;


    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String role = userDetails
                .getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(ROL.USER.name());

        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String refreshToken(String token) {
        if(isTokenExpired(token)){
            throw new JwtException("Token expired");
        }
        Claims claims = excractAllClaims(token);
        return Jwts.builder()
                .claims(claims)
                .subject(claims.getSubject())
                .issuedAt(new Date((System.currentTimeMillis())))
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims excractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        try{
            return excractAllClaims(token).getSubject();
        }catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try{
            Claims claims = excractAllClaims(token);
            String role = claims.get("role", String.class);
            return role.startsWith("ROLE_") ? role : "ROLE_" + role;
        }catch (JwtException | IllegalArgumentException e) {
            return "ROLE_USER";
        }
    }

    private Date extractExpiration(String token) {
        try{
            return excractAllClaims(token).getExpiration();
        }catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean isTokenValid(String token) {
        try{
            String email = extractEmail(token);
            return email!= null && !isTokenExpired(email);
        }catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}

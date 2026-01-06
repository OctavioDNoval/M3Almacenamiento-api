package com.example.m3almacenamiento.configuracion;

import com.example.m3almacenamiento.modelo.enumerados.ROL;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String refreshToken(String token) {
        if(isTokenExpired(token)){
            throw new JwtException("Token expired");
        }
        Claims claims = excractAllClaims(token);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.getSubject())
                .setIssuedAt(new Date((System.currentTimeMillis())))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims excractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
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

    public Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) {
            return true; // Si no tiene expiración, considéralo expirado
        }
        return expiration.before(new Date());
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean isTokenValid(String token) {
        try{
            String email = extractEmail(token);
            return email!= null && !isTokenExpired(token);
        }catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}

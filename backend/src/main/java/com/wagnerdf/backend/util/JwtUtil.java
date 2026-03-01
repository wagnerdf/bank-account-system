package com.wagnerdf.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.wagnerdf.backend.model.UserAccount;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "000111222333444555666777888999000"; 
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hora

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 🔐 Gera token com dados do usuário
    public String generateToken(UserAccount user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("role", user.getRole().getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 📥 Extrai email (subject)
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 📥 Extrai role
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 📥 Extrai id
    public Long extractUserId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    // 🔎 Valida token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 🔧 Método interno para pegar Claims
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
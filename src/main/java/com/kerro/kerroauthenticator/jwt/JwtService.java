package com.kerro.kerroauthenticator.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationMillis;

    public JwtService(@Value("${security.jwt.secret-key}") String secret,
                      @Value("${security.jwt.expiration-time:3600000}") long expirationMillis) {
        byte[] keyBytes = decodeSecret(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMillis = expirationMillis;
    }

    private byte[] decodeSecret(String secret) {
        if (secret == null) {
            throw new IllegalArgumentException("JWT secret is null");
        }
        // Intentar Base64
        try {
            return Decoders.BASE64.decode(secret);
        } catch (Exception ignored) { }

        // Intentar hex
        try {
            return hexStringToByteArray(secret);
        } catch (Exception ignored) { }

        // Fallback: bytes UTF-8
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] hexStringToByteArray(String s) {
        String hex = s.startsWith("0x") ? s.substring(2) : s;
        int len = hex.length();
        if ((len & 1) != 0) {
            // si longitud impar, prefijar 0
            hex = "0" + hex;
            len = hex.length();
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) throws JwtException {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public long getExpirationEpochMillis(String token) throws JwtException {
        Claims claims = extractAllClaims(token);
        Date exp = claims.getExpiration();
        if (exp == null) {
            throw new IllegalArgumentException("Token does not contain expiration claim");
        }
        return exp.toInstant().toEpochMilli();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.toInstant().isAfter(Instant.now());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String generateToken(UserDetails user) {
        return generateToken(user, this.expirationMillis);
    }

    public String generateToken(UserDetails user, long ttlMillis) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiry = Date.from(now.plusMillis(ttlMillis));

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiry)
                .signWith(signingKey)
                .compact();
    }
}
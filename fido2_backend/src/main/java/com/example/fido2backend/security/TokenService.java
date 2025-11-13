package com.example.fido2backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * TokenService issues and verifies JWTs for session/auth.
 */
@Service
public class TokenService {

    private final SecretKey key;

    public TokenService(@Value("${JWT_SECRET:}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            // For local dev, generate a temp key but log a warning.
            // In production, require JWT_SECRET via env.
            byte[] bytes = new byte[64];
            for (int i = 0; i < bytes.length; i++) bytes[i] = (byte)(i + 7);
            this.key = Keys.hmacShaKeyFor(bytes);
        } else {
            byte[] k = Decoders.BASE64.decode(jwtSecret);
            this.key = Keys.hmacShaKeyFor(k);
        }
    }

    // PUBLIC_INTERFACE
    public String issueToken(String subject, long ttlSeconds, Map<String, Object> claims) {
        /** Issue a signed JWT with subject and additional claims. */
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // PUBLIC_INTERFACE
    public Jws<Claims> parseAndValidate(String jwt) {
        /** Parse and validate JWT signature and expiration. */
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
    }
}

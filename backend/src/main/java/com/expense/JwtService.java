package com.expense;

import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    public JwtService(@Value("${app.jwt.secret}") String secret) {
        try { this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("JWT_SECRET must be a base64-encoded key of at least 32 bytes", ex); }
    }
    public String create(String userId) {
        return Jwts.builder().subject(userId).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 86_400_000L)).signWith(key).compact();
    }
    public String userId(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
}

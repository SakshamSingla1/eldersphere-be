package com.eldersphere.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String email, String userId) {
        return generateAccessToken(email, userId, null, Collections.emptyList());
    }

    /**
     * @param primaryRole the caller's current primary/default role (users.user_type), kept as a
     *                     single-value "role" claim so existing token-parsing code isn't broken
     * @param roles       the full set of UserTypeEnum roles the caller holds (multi-role support)
     */
    public String generateAccessToken(String email, String userId, String primaryRole, List<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", primaryRole)
                .claim("roles", roles != null ? roles : Collections.emptyList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractUserId(String token) {
        Claims claims = getClaims(token);
        Object userId = claims.get("userId");
        if (userId == null) return null;
        return userId.toString();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Claims claims = getClaims(token);
            Object roles = claims.get("roles");
            if (roles instanceof List<?>) {
                return ((List<Object>) roles).stream().map(String::valueOf).toList();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean validateToken(String token, String email) {
        try {
            Claims claims = getClaims(token);
            return email.equals(claims.getSubject())
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        String cleanToken = token.trim();
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(cleanToken)
                .getPayload();
    }
}

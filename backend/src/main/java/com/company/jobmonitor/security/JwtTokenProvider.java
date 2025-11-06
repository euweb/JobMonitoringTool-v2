package com.company.jobmonitor.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${spring.security.jwt.access-token-validity}") long accessTokenValidity,
            @Value("${spring.security.jwt.refresh-token-validity}") long refreshTokenValidity) {
        
        // Ensure secret is long enough for HS512
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidity;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidity;
    }

    public String createAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Instant now = Instant.now();
        Instant validity = now.plus(accessTokenValidityInMilliseconds, ChronoUnit.MILLIS);

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userPrincipal.getUsername());
        claims.put("userId", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("authorities", authorities);
        claims.put("tokenType", "ACCESS");

        return Jwts.builder()
                .claims(claims)
                .subject(userPrincipal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Instant now = Instant.now();
        Instant validity = now.plus(refreshTokenValidityInMilliseconds, ChronoUnit.MILLIS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userPrincipal.getUsername());
        claims.put("userId", userPrincipal.getId());
        claims.put("tokenType", "REFRESH");

        return Jwts.builder()
                .claims(claims)
                .subject(userPrincipal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            throw e;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Integer) {
            return (Integer) userIdObj;
        } else if (userIdObj instanceof Long) {
            return ((Long) userIdObj).intValue();
        } else if (userIdObj instanceof Number) {
            return ((Number) userIdObj).intValue();
        }
        throw new IllegalArgumentException("Invalid userId type in token");
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return "ACCESS".equals(claims.get("tokenType", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return "REFRESH".equals(claims.get("tokenType", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    public long getAccessTokenValidityInMilliseconds() {
        return accessTokenValidityInMilliseconds;
    }

    public String generateToken(org.springframework.security.core.userdetails.UserDetails userDetails) {
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        
        Instant now = Instant.now();
        Instant validity = now.plus(accessTokenValidityInMilliseconds, ChronoUnit.MILLIS);

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userPrincipal.getUsername());
        claims.put("userId", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("authorities", authorities);
        claims.put("tokenType", "ACCESS");

        return Jwts.builder()
                .claims(claims)
                .subject(userPrincipal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }
}
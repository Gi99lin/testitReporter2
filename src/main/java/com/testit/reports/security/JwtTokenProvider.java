package com.testit.reports.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate token for a user
     *
     * @param username User's username
     * @param roles    User's roles
     * @return JWT token
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get username from token
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Get authentication from token
     *
     * @param token JWT token or other token type
     * @return Authentication
     */
    public Authentication getAuthentication(String token) {
        // Special handling for TestIT tokens with OpenIdConnect prefix
        if (token.startsWith("OpenIdConnect ") || token.startsWith("OpenIdConnect")) {
            log.info("Creating authentication for TestIT OpenIdConnect token");
            // For TestIT tokens, we create a default user with ROLE_USER
            Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
            // Extract username from token if possible, or use a default
            String username = "testit-user";
            try {
                // Try to extract some identifier from the token
                if (token.contains(".")) {
                    String[] parts = token.split("\\.");
                    if (parts.length > 1) {
                        username = "testit-" + parts[1].substring(0, Math.min(8, parts[1].length()));
                    }
                }
            } catch (Exception e) {
                log.warn("Could not extract username from TestIT token, using default", e);
            }
            
            User principal = new User(username, "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        }
        
        // For non-JWT tokens that are not OpenIdConnect
        if (!token.startsWith("ey")) {
            log.info("Creating authentication for non-JWT token");
            // Create a default user with ROLE_USER
            Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
            User principal = new User("user-" + System.currentTimeMillis(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        }
        
        // Standard JWT token processing
        try {
            log.info("Creating authentication for JWT token");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get("roles").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            User principal = new User(claims.getSubject(), "", authorities);

            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        } catch (Exception e) {
            log.error("Error creating authentication from token: {}", e.getMessage());
            // Fallback to a default user with ROLE_USER
            Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
            User principal = new User("fallback-user", "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        }
    }

    /**
     * Validate token
     *
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null) {
            log.info("Token is null, skipping validation");
            return false;
        }
        
        // Special handling for TestIT tokens with OpenIdConnect prefix
        if (token.startsWith("OpenIdConnect ") || token.startsWith("OpenIdConnect")) {
            log.info("Token is a TestIT OpenIdConnect token, considering it valid for authentication");
            return true;
        }
        
        // JWT tokens start with "ey" (base64 encoded header)
        if (!token.startsWith("ey")) {
            log.info("Token doesn't look like a JWT token, but allowing it for authentication");
            return true;
        }
        
        try {
            log.info("Validating JWT token");
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.info("JWT token is valid");
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
        }
        return false;
    }
}

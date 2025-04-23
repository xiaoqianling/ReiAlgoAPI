package com.rei.algo.security;

import com.rei.algo.model.entity.User; // Assuming User implements UserDetails
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Use the correct SignatureException
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}") // 从 application.yml 读取密钥
    private String jwtSecretString;

    @Value("${app.jwt.expiration-ms}") // 从 application.yml 读取有效期
    private int jwtExpirationInMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Ensure the secret string is strong enough for the algorithm (HS256 requires at least 256 bits / 32 bytes)
        // In production, use a securely generated and stored key.
        if (!StringUtils.hasText(jwtSecretString) || jwtSecretString.getBytes().length < 32) { // Check byte length
            log.warn("JWT Secret key is missing or too short (requires 32+ bytes) in configuration. Generating a temporary secure key. THIS IS NOT SUITABLE FOR PRODUCTION.");
            // Generate a secure key if not configured properly (for development only)
             this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
             // Use the configured secret string (assuming it's Base64 encoded or directly usable)
             // Keys.hmacShaKeyFor is safer as it handles different key formats properly.
             this.secretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
        }

        if (jwtExpirationInMs <= 0) {
            log.warn("JWT expiration time (app.jwt.expiration-ms) is not configured or invalid. Using default: 1 hour.");
            this.jwtExpirationInMs = 3600000; // Default to 1 hour
        }
    }

    // 生成 JWT 令牌
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String userId = "";
        if (userPrincipal instanceof User) {
             userId = ((User) userPrincipal).getUserId(); // Assuming getUserId() exists in User entity
        } else {
            // Handle cases where principal might not be your User entity directly
            // Potentially extract ID from claims if available, or throw error
             log.warn("User principal is not an instance of com.rei.algo.model.entity.User. Cannot extract custom userId claim reliably.");
             // Consider extracting username as fallback or throwing an error
             // userId = userPrincipal.getUsername(); // Example fallback
        }


        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Usually username
                .claim("userId", userId) // Add custom claim for user ID
                // Add other claims if needed (e.g., roles using getAuthorities())
                // .claim("roles", userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 从 JWT 令牌中获取用户名
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // 从 JWT 令牌中获取用户ID (自定义声明)
     public String getUserIdFromJWT(String token) {
         Claims claims = Jwts.parserBuilder()
                 .setSigningKey(secretKey)
                 .build()
                 .parseClaimsJws(token)
                 .getBody();

         // Safely get the userId claim
         return claims.get("userId", String.class);
     }


    // 验证 JWT 令牌
    public boolean validateToken(String authToken) {
        if (!StringUtils.hasText(authToken)) {
             //log.debug("JWT token is empty"); // Optional logging
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
} 
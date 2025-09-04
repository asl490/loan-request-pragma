package com.pragma.bootcamp.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.SignatureException;
import java.util.Date;
import java.util.function.Function;
@Slf4j
@Component
public class JwtProvider implements Serializable {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

//    public Claims getAllClaimsFromToken(String token) {
//        if (secretKey == null) {
//            secretKey = Keys.hmacShaKeyFor(secret.getBytes());
//        }
//        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
//    }
    public Mono<Claims> getClaimsFromToken(String token) {

        return Mono.fromCallable(() -> Jwts.parser()
                        .verifyWith(getKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload())
                .onErrorMap(this::mapJwtException);
    }
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Mono<Boolean> validateToken(String token) {
        return getClaimsFromToken(token)
                .map(claims -> true)
                .onErrorResume(e -> {
                    log.warn("Token validation failed: {}", e.getMessage());
                    return Mono.just(false);
                });
    }

//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
    private Throwable mapJwtException(Throwable ex) {
        String message = switch (ex) {
            case SecurityException ignored -> {
                log.debug("Invalid JWT signature for token");
                yield "Invalid JWT signature";
            }
            case ExpiredJwtException ignored -> {
                log.debug("Expired JWT token");
                yield "Token has expired";
            }
            case UnsupportedJwtException ignored -> {
                log.debug("Unsupported JWT token");
                yield "Unsupported JWT token";
            }
            case MalformedJwtException ignored -> {
                log.debug("Malformed JWT token");
                yield "Malformed JWT token";
            }
            case IllegalArgumentException ignored -> {
                log.debug("Empty or invalid JWT token");
                yield "Invalid JWT token";
            }
            case SignatureException ignored -> {
                log.debug("JWT signature does not match locally computed signature {}", ex.getMessage());
                yield "JWT signature does not match locally computed signature";
            }
            default -> {
                log.warn("Unexpected JWT parsing error {}", ex.getMessage());
                yield "JWT processing error";
            }
        };

        return new JwtException(message, ex);
    }

//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }

//    private Boolean isTokenExpired(String token) {
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }

//    public Boolean validateToken(String token) {
//        // For this context, we only care if the token is not expired and well-formed.
//        // The signature validation is handled by getAllClaimsFromToken.
//        // In a real scenario, you might also check against a user details service.
//        return !isTokenExpired(token);
//    }
}

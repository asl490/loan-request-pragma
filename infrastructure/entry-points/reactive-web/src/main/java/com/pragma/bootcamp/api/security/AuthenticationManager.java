package com.pragma.bootcamp.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtProvider jwtProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        return jwtProvider.validateToken(authToken)
                .filter(valid -> valid)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid or expired JWT token")))
                .then(jwtProvider.getClaimsFromToken(authToken))
                .map(this::buildAuthenticationFromClaims)
                .onErrorMap(JwtException.class, ex ->
                        new BadCredentialsException("Invalid JWT token", ex))
                .onErrorMap(ex ->
                        new BadCredentialsException("Authentication failed", ex));
    }

    private Authentication buildAuthenticationFromClaims(Claims claims) {
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }


//    @Override
//    @SuppressWarnings("unchecked")
//    public Mono<Authentication> authenticate(Authentication authentication) {
//        String authToken = authentication.getCredentials().toString();
//
//        if (jwtProvider.validateToken(authToken)) {
//            Claims claims = jwtProvider.getAllClaimsFromToken(authToken);
//            List<String> roles = claims.get("roles", List.class);
//            List<SimpleGrantedAuthority> authorities = roles.stream()
//                    .map(SimpleGrantedAuthority::new)
//                    .collect(Collectors.toList());
//
//            UsernamePasswordAuthenticationToken auth =
//                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
//            return Mono.just(auth);
//        } else {
//            return Mono.empty();
//        }
//    }
}

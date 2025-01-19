package com.asyncgate.apigateway_server.filter;

import com.asyncgate.apigateway_server.jwt.JwtTokenTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final JwtTokenTokenUtil jwtTokenUtils;

    @Override
    public GatewayFilter apply(final Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(response, "JWT token is missing", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return onError(response, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }
            String jwtToken = authorizationHeader.substring(7);

            if (!jwtTokenUtils.validate(jwtToken)) {
                return onError(response, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }

            // 클레임 추출
            Claims claims = jwtTokenUtils.extractClaims(jwtToken);
            String id = claims.getSubject();
            if (id == null) {
                return onError(response, "JWT claims are invalid", HttpStatus.UNAUTHORIZED);
            }

            // 헤더에 사용자 정보 추가
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("id", id)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        log.error("Authorization error: {}", message);
        return response.setComplete();
    }

    public static class Config {

    }
}

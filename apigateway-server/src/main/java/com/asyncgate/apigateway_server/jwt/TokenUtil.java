package com.asyncgate.apigateway_server.jwt;

import io.jsonwebtoken.Claims;

public interface TokenTokenUtil {
    boolean validate(String token);

    Claims extractClaims(String token);
}

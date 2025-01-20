package com.asyncgate.chat_server.util;

import io.jsonwebtoken.Claims;

public interface TokenUtil {
    boolean validate(String token);

    Claims extractClaims(String token);
}

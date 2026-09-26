package com.chongchao.mvp.auth;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public final class BearerTokenExtractor {

    private static final String PREFIX = "Bearer ";

    private BearerTokenExtractor() {
    }

    public static Optional<String> extract(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(PREFIX)) {
            return Optional.empty();
        }
        String token = authorization.substring(PREFIX.length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }
}


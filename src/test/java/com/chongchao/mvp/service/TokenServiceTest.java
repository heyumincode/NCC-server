package com.chongchao.mvp.service;

import com.chongchao.mvp.auth.TokenService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Test
    void shouldCreateDifferentOpaqueTokens() {
        String first = tokenService.newToken();
        String second = tokenService.newToken();

        assertThat(first).hasSizeGreaterThanOrEqualTo(40);
        assertThat(second).isNotEqualTo(first);
        assertThat(tokenService.hash(first)).hasSize(64);
    }

    @Test
    void shouldCompareSecretsInConstantTimeApi() {
        assertThat(tokenService.constantTimeEquals("same", "same")).isTrue();
        assertThat(tokenService.constantTimeEquals("same", "other")).isFalse();
        assertThat(tokenService.constantTimeEquals(null, "other")).isFalse();
    }
}


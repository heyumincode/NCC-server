package com.chongchao.mvp.dto;

import java.time.Instant;

public record WechatLoginResponse(
        String token,
        Instant expiresAt,
        long userId,
        String nickname,
        String avatarUrl
) {
}


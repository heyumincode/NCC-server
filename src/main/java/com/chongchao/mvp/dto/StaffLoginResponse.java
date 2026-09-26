package com.chongchao.mvp.dto;

import java.time.Instant;

public record StaffLoginResponse(String token, Instant expiresAt, String staffName) {
}


package com.chongchao.mvp.dto;

public record AdminMatchStatsResponse(
        long matchEventId,
        int capacity,
        int remaining,
        long reserved,
        long verified,
        long cancelled
) {
}


package com.chongchao.mvp.domain;

import java.time.Instant;

public record MatchEvent(
        long id,
        String competitionName,
        String roundName,
        String homeTeam,
        String awayTeam,
        String venue,
        String venueAddress,
        Instant kickoffAt,
        Instant admissionAt,
        Instant bookingStartAt,
        Instant bookingEndAt,
        int capacity,
        int reservedCount,
        MatchStatus status,
        String coverImageUrl,
        String notice
) {
    public int remainingCount() {
        return Math.max(0, capacity - reservedCount);
    }
}


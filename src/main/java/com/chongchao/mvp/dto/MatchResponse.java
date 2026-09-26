package com.chongchao.mvp.dto;

import com.chongchao.mvp.domain.MatchEvent;
import com.chongchao.mvp.domain.MatchStatus;

import java.time.Instant;

public record MatchResponse(
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
        int remainingCount,
        MatchStatus status,
        String coverImageUrl,
        String notice
) {
    public static MatchResponse from(MatchEvent event) {
        return new MatchResponse(
                event.id(),
                event.competitionName(),
                event.roundName(),
                event.homeTeam(),
                event.awayTeam(),
                event.venue(),
                event.venueAddress(),
                event.kickoffAt(),
                event.admissionAt(),
                event.bookingStartAt(),
                event.bookingEndAt(),
                event.capacity(),
                event.reservedCount(),
                event.remainingCount(),
                event.status(),
                event.coverImageUrl(),
                event.notice()
        );
    }
}


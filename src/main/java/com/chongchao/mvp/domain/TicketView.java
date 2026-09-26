package com.chongchao.mvp.domain;

import java.time.Instant;

public record TicketView(
        long id,
        String orderNo,
        long matchEventId,
        String competitionName,
        String roundName,
        String homeTeam,
        String awayTeam,
        String venue,
        String venueAddress,
        Instant kickoffAt,
        Instant admissionAt,
        String attendeeName,
        String attendeePhone,
        TicketStatus status,
        String ticketCode,
        Instant reservedAt,
        Instant verifiedAt,
        String verifiedGate,
        String verifiedBy
) {
}


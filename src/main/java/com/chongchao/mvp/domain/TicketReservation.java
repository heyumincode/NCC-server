package com.chongchao.mvp.domain;

import java.time.Instant;

public record TicketReservation(
        long id,
        String orderNo,
        long matchEventId,
        long appUserId,
        String attendeeName,
        String attendeePhone,
        TicketStatus status,
        String ticketCode,
        Instant reservedAt,
        Instant cancelledAt,
        Instant verifiedAt,
        String verifiedGate,
        String verifiedBy
) {
}


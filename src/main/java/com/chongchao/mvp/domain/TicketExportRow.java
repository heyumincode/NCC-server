package com.chongchao.mvp.domain;

import java.time.Instant;

public record TicketExportRow(
        String orderNo,
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


package com.chongchao.mvp.dto;

import com.chongchao.mvp.domain.VerificationResult;

import java.time.Instant;

public record VerifyTicketResponse(
        VerificationResult result,
        String message,
        String ticketCode,
        String attendeeName,
        String homeTeam,
        String awayTeam,
        Instant kickoffAt,
        Instant verifiedAt,
        String verifiedGate
) {
}


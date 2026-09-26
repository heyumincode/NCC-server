package com.chongchao.mvp.dto;

import com.chongchao.mvp.domain.TicketStatus;
import com.chongchao.mvp.domain.TicketView;

import java.time.Instant;

public record TicketResponse(
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
        String attendeePhoneMasked,
        TicketStatus status,
        String ticketCode,
        Instant reservedAt,
        Instant verifiedAt,
        String verifiedGate,
        String qrImageBase64
) {
    public static TicketResponse from(TicketView view, String qrImageBase64) {
        return new TicketResponse(
                view.id(),
                view.orderNo(),
                view.matchEventId(),
                view.competitionName(),
                view.roundName(),
                view.homeTeam(),
                view.awayTeam(),
                view.venue(),
                view.venueAddress(),
                view.kickoffAt(),
                view.admissionAt(),
                view.attendeeName(),
                maskPhone(view.attendeePhone()),
                view.status(),
                view.ticketCode(),
                view.reservedAt(),
                view.verifiedAt(),
                view.verifiedGate(),
                qrImageBase64
        );
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}


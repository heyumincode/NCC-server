package com.chongchao.mvp.service;

import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.config.AppProperties;
import com.chongchao.mvp.domain.AppUser;
import com.chongchao.mvp.domain.MatchEvent;
import com.chongchao.mvp.domain.MatchStatus;
import com.chongchao.mvp.domain.TicketReservation;
import com.chongchao.mvp.domain.TicketStatus;
import com.chongchao.mvp.domain.TicketView;
import com.chongchao.mvp.dto.ReserveTicketRequest;
import com.chongchao.mvp.dto.TicketResponse;
import com.chongchao.mvp.repository.MatchEventRepository;
import com.chongchao.mvp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private MatchEventRepository matchEventRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private QrCodeService qrCodeService;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(
                "admin", "staff", 168, 12, "CHONGCHAO:TICKET:",
                new AppProperties.Wechat("app", "secret", true)
        );
        ticketService = new TicketService(matchEventRepository, ticketRepository, qrCodeService, properties);
    }

    @Test
    void shouldReturnExistingTicketEvenWhenMatchIsSoldOut() {
        AppUser user = new AppUser(7L, "openid", "", "", true);
        MatchEvent match = match(MatchStatus.SOLD_OUT, 100, 100);
        TicketReservation existing = reservation(21L, user.id(), match.id(), TicketStatus.RESERVED);
        TicketView view = ticketView(existing, match);
        when(matchEventRepository.findByIdForUpdate(match.id())).thenReturn(Optional.of(match));
        when(ticketRepository.findActiveByUserAndMatch(user.id(), match.id())).thenReturn(Optional.of(existing));
        when(ticketRepository.findViewByIdAndUser(existing.id(), user.id())).thenReturn(Optional.of(view));
        when(qrCodeService.createBase64Png(anyString())).thenReturn("qr-base64");

        TicketResponse response = ticketService.reserve(
                user,
                new ReserveTicketRequest(match.id(), "测试观众", "13800138000", true)
        );

        assertThat(response.id()).isEqualTo(existing.id());
        verify(ticketRepository, never()).create(anyString(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), anyString(), anyString(), anyString());
        verify(matchEventRepository, never()).incrementReservedCount(match.id());
    }

    @Test
    void shouldRejectNewReservationWhenSoldOut() {
        AppUser user = new AppUser(7L, "openid", "", "", true);
        MatchEvent match = match(MatchStatus.SOLD_OUT, 100, 100);
        when(matchEventRepository.findByIdForUpdate(match.id())).thenReturn(Optional.of(match));
        when(ticketRepository.findActiveByUserAndMatch(user.id(), match.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.reserve(
                user,
                new ReserveTicketRequest(match.id(), "测试观众", "13800138000", true)
        )).isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo("SOLD_OUT");
    }

    private MatchEvent match(MatchStatus status, int capacity, int reservedCount) {
        Instant now = Instant.now();
        return new MatchEvent(
                11L, "充超联赛", "揭幕战", "顺庆代表队", "高坪代表队",
                "南充市体育中心", "南充市", now.plus(7, ChronoUnit.DAYS),
                now.plus(7, ChronoUnit.DAYS).minus(90, ChronoUnit.MINUTES),
                now.minus(1, ChronoUnit.DAYS), now.plus(6, ChronoUnit.DAYS),
                capacity, reservedCount, status, "", "观赛须知"
        );
    }

    private TicketReservation reservation(long id, long userId, long matchId, TicketStatus status) {
        return new TicketReservation(
                id, "CC202609240001", matchId, userId, "测试观众", "13800138000",
                status, "CC-ABC234-DEF567", Instant.now(), null, null, "", ""
        );
    }

    private TicketView ticketView(TicketReservation reservation, MatchEvent match) {
        return new TicketView(
                reservation.id(), reservation.orderNo(), match.id(), match.competitionName(), match.roundName(),
                match.homeTeam(), match.awayTeam(), match.venue(), match.venueAddress(), match.kickoffAt(),
                match.admissionAt(), reservation.attendeeName(), reservation.attendeePhone(), reservation.status(),
                reservation.ticketCode(), reservation.reservedAt(), reservation.verifiedAt(),
                reservation.verifiedGate(), reservation.verifiedBy()
        );
    }
}


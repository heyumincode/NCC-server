package com.chongchao.mvp.service;

import com.chongchao.mvp.auth.TokenService;
import com.chongchao.mvp.config.AppProperties;
import com.chongchao.mvp.domain.TicketReservation;
import com.chongchao.mvp.domain.TicketStatus;
import com.chongchao.mvp.domain.TicketView;
import com.chongchao.mvp.domain.VerificationResult;
import com.chongchao.mvp.dto.VerifyTicketRequest;
import com.chongchao.mvp.dto.VerifyTicketResponse;
import com.chongchao.mvp.repository.StaffSessionRepository;
import com.chongchao.mvp.repository.TicketRepository;
import com.chongchao.mvp.repository.VerificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffSessionRepository staffSessionRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private VerificationLogRepository verificationLogRepository;

    private StaffService staffService;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(
                "admin", "staff-passcode", 168, 12, "CHONGCHAO:TICKET:",
                new AppProperties.Wechat("app", "secret", true)
        );
        staffService = new StaffService(
                properties,
                new TokenService(),
                staffSessionRepository,
                ticketRepository,
                verificationLogRepository
        );
    }

    @Test
    void shouldRejectDuplicateVerificationAndReturnFirstRecord() {
        Instant verifiedAt = Instant.now().minusSeconds(60);
        TicketReservation ticket = new TicketReservation(
                1L, "ORDER", 2L, 3L, "测试观众", "13800138000",
                TicketStatus.VERIFIED, "CC-ABC234-DEF567", Instant.now().minusSeconds(3600),
                null, verifiedAt, "A入口", "核销员甲"
        );
        TicketView view = new TicketView(
                1L, "ORDER", 2L, "充超联赛", "揭幕战", "顺庆代表队", "高坪代表队",
                "南充市体育中心", "南充市", Instant.now().plusSeconds(7200), Instant.now().plusSeconds(3600),
                "测试观众", "13800138000", TicketStatus.VERIFIED, "CC-ABC234-DEF567",
                ticket.reservedAt(), verifiedAt, "A入口", "核销员甲"
        );
        when(ticketRepository.findByTicketCodeForUpdate(ticket.ticketCode())).thenReturn(Optional.of(ticket));
        when(ticketRepository.findViewByTicketCode(ticket.ticketCode())).thenReturn(Optional.of(view));

        VerifyTicketResponse response = staffService.verify(
                "核销员乙",
                new VerifyTicketRequest("CHONGCHAO:TICKET:" + ticket.ticketCode(), "B入口")
        );

        assertThat(response.result()).isEqualTo(VerificationResult.ALREADY_VERIFIED);
        assertThat(response.verifiedAt()).isEqualTo(verifiedAt);
        assertThat(response.verifiedGate()).isEqualTo("A入口");
        verify(verificationLogRepository).create(
                ticket.id(), ticket.ticketCode(), VerificationResult.ALREADY_VERIFIED,
                "B入口", "核销员乙", "重复核销"
        );
    }
}


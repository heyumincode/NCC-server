package com.chongchao.mvp.service;

import com.chongchao.mvp.auth.TokenService;
import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.config.AppProperties;
import com.chongchao.mvp.domain.TicketReservation;
import com.chongchao.mvp.domain.TicketStatus;
import com.chongchao.mvp.domain.TicketView;
import com.chongchao.mvp.domain.VerificationResult;
import com.chongchao.mvp.dto.StaffLoginRequest;
import com.chongchao.mvp.dto.StaffLoginResponse;
import com.chongchao.mvp.dto.VerifyTicketRequest;
import com.chongchao.mvp.dto.VerifyTicketResponse;
import com.chongchao.mvp.repository.StaffSessionRepository;
import com.chongchao.mvp.repository.TicketRepository;
import com.chongchao.mvp.repository.VerificationLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class StaffService {

    private static final Pattern TICKET_CODE_PATTERN = Pattern.compile("^[A-Z0-9-]{10,48}$");

    private final AppProperties appProperties;
    private final TokenService tokenService;
    private final StaffSessionRepository staffSessionRepository;
    private final TicketRepository ticketRepository;
    private final VerificationLogRepository verificationLogRepository;

    public StaffService(
            AppProperties appProperties,
            TokenService tokenService,
            StaffSessionRepository staffSessionRepository,
            TicketRepository ticketRepository,
            VerificationLogRepository verificationLogRepository
    ) {
        this.appProperties = appProperties;
        this.tokenService = tokenService;
        this.staffSessionRepository = staffSessionRepository;
        this.ticketRepository = ticketRepository;
        this.verificationLogRepository = verificationLogRepository;
    }

    @Transactional
    public StaffLoginResponse login(StaffLoginRequest request) {
        String configuredPasscode = appProperties.staffPasscode();
        if (configuredPasscode == null || configuredPasscode.isBlank()) {
            throw new BusinessException("STAFF_NOT_CONFIGURED", "工作人员登录尚未配置", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (!tokenService.constantTimeEquals(configuredPasscode, request.passcode())) {
            throw new BusinessException("STAFF_PASSCODE_INVALID", "核销口令不正确", HttpStatus.UNAUTHORIZED);
        }
        String token = tokenService.newToken();
        Instant expiresAt = Instant.now().plus(appProperties.staffSessionHours(), ChronoUnit.HOURS);
        String staffName = request.staffName().trim();
        staffSessionRepository.create(staffName, tokenService.hash(token), expiresAt);
        return new StaffLoginResponse(token, expiresAt, staffName);
    }

    @Transactional
    public VerifyTicketResponse verify(String staffName, VerifyTicketRequest request) {
        String ticketCode = extractTicketCode(request.payload());
        if (ticketCode == null) {
            verificationLogRepository.create(
                    null,
                    safeLogCode(request.payload()),
                    VerificationResult.INVALID,
                    request.gateName(),
                    staffName,
                    "二维码格式不正确"
            );
            return new VerifyTicketResponse(
                    VerificationResult.INVALID,
                    "二维码不是充超赛事电子票",
                    "",
                    "",
                    "",
                    "",
                    null,
                    null,
                    ""
            );
        }

        Optional<TicketReservation> optionalTicket = ticketRepository.findByTicketCodeForUpdate(ticketCode);
        if (optionalTicket.isEmpty()) {
            verificationLogRepository.create(
                    null,
                    ticketCode,
                    VerificationResult.NOT_FOUND,
                    request.gateName(),
                    staffName,
                    "票码不存在"
            );
            return emptyTicketResult(VerificationResult.NOT_FOUND, "未找到该电子票", ticketCode);
        }

        TicketReservation ticket = optionalTicket.get();
        if (ticket.status() == TicketStatus.CANCELLED) {
            verificationLogRepository.create(
                    ticket.id(), ticketCode, VerificationResult.CANCELLED,
                    request.gateName(), staffName, "电子票已取消"
            );
            return viewResult(ticketCode, VerificationResult.CANCELLED, "该电子票已取消");
        }
        if (ticket.status() == TicketStatus.VERIFIED) {
            verificationLogRepository.create(
                    ticket.id(), ticketCode, VerificationResult.ALREADY_VERIFIED,
                    request.gateName(), staffName, "重复核销"
            );
            return viewResult(ticketCode, VerificationResult.ALREADY_VERIFIED, "该电子票已经核销，请勿重复放行");
        }

        ticketRepository.markVerified(ticket.id(), request.gateName(), staffName);
        verificationLogRepository.create(
                ticket.id(), ticketCode, VerificationResult.VERIFIED,
                request.gateName(), staffName, "核销成功"
        );
        return viewResult(ticketCode, VerificationResult.VERIFIED, "核销成功");
    }

    private VerifyTicketResponse viewResult(String ticketCode, VerificationResult result, String message) {
        TicketView view = ticketRepository.findViewByTicketCode(ticketCode)
                .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "电子票不存在", HttpStatus.NOT_FOUND));
        return new VerifyTicketResponse(
                result,
                message,
                ticketCode,
                view.attendeeName(),
                view.homeTeam(),
                view.awayTeam(),
                view.kickoffAt(),
                view.verifiedAt(),
                view.verifiedGate()
        );
    }

    private VerifyTicketResponse emptyTicketResult(VerificationResult result, String message, String ticketCode) {
        return new VerifyTicketResponse(result, message, ticketCode, "", "", "", null, null, "");
    }

    private String extractTicketCode(String payload) {
        String value = payload == null ? "" : payload.trim().toUpperCase();
        String prefix = appProperties.ticketQrPrefix().toUpperCase();
        if (value.startsWith(prefix)) {
            value = value.substring(prefix.length());
        }
        return TICKET_CODE_PATTERN.matcher(value).matches() ? value : null;
    }

    private String safeLogCode(String payload) {
        if (payload == null) {
            return "INVALID";
        }
        String value = payload.trim();
        return value.length() <= 48 ? value : value.substring(0, 48);
    }
}


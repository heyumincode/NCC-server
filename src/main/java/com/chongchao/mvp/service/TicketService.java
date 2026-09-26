package com.chongchao.mvp.service;

import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.config.AppProperties;
import com.chongchao.mvp.domain.AppUser;
import com.chongchao.mvp.domain.MatchEvent;
import com.chongchao.mvp.domain.MatchStatus;
import com.chongchao.mvp.domain.TicketReservation;
import com.chongchao.mvp.domain.TicketView;
import com.chongchao.mvp.dto.ReserveTicketRequest;
import com.chongchao.mvp.dto.TicketResponse;
import com.chongchao.mvp.repository.MatchEventRepository;
import com.chongchao.mvp.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TicketService {

    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneOffset.UTC);
    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final MatchEventRepository matchEventRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public TicketService(
            MatchEventRepository matchEventRepository,
            TicketRepository ticketRepository,
            QrCodeService qrCodeService,
            AppProperties appProperties
    ) {
        this.matchEventRepository = matchEventRepository;
        this.ticketRepository = ticketRepository;
        this.qrCodeService = qrCodeService;
        this.appProperties = appProperties;
    }

    @Transactional
    public TicketResponse reserve(AppUser appUser, ReserveTicketRequest request) {
        MatchEvent match = matchEventRepository.findByIdForUpdate(request.matchEventId())
                .orElseThrow(() -> new BusinessException("MATCH_NOT_FOUND", "赛事不存在", HttpStatus.NOT_FOUND));
        TicketReservation existing = ticketRepository.findActiveByUserAndMatch(appUser.id(), match.id()).orElse(null);
        if (existing != null) {
            return detail(appUser, existing.id());
        }
        validateBookable(match, Instant.now());

        String ticketCode = createTicketCode();
        TicketReservation created = ticketRepository.create(
                createOrderNo(),
                match.id(),
                appUser.id(),
                request.attendeeName().trim(),
                request.attendeePhone(),
                ticketCode
        );
        matchEventRepository.incrementReservedCount(match.id());
        return detail(appUser, created.id());
    }

    public List<TicketResponse> list(AppUser appUser) {
        return ticketRepository.findViewsByUser(appUser.id()).stream()
                .map(view -> TicketResponse.from(view, null))
                .toList();
    }

    public TicketResponse detail(AppUser appUser, long ticketId) {
        TicketView view = ticketRepository.findViewByIdAndUser(ticketId, appUser.id())
                .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "电子票不存在", HttpStatus.NOT_FOUND));
        String payload = appProperties.ticketQrPrefix() + view.ticketCode();
        return TicketResponse.from(view, qrCodeService.createBase64Png(payload));
    }

    private void validateBookable(MatchEvent match, Instant now) {
        if (match.status() != MatchStatus.PUBLISHED) {
            String code = match.status() == MatchStatus.SOLD_OUT ? "SOLD_OUT" : "MATCH_NOT_BOOKABLE";
            throw new BusinessException(code, "当前赛事不可预约", HttpStatus.CONFLICT);
        }
        if (now.isBefore(match.bookingStartAt())) {
            throw new BusinessException("BOOKING_NOT_STARTED", "预约尚未开始", HttpStatus.CONFLICT);
        }
        if (!now.isBefore(match.bookingEndAt())) {
            throw new BusinessException("BOOKING_ENDED", "预约已经结束", HttpStatus.CONFLICT);
        }
        if (match.remainingCount() <= 0) {
            throw new BusinessException("SOLD_OUT", "本场预约名额已满", HttpStatus.CONFLICT);
        }
    }

    private String createOrderNo() {
        return "CC" + ORDER_DATE_FORMAT.format(Instant.now()) + randomCode(8);
    }

    private String createTicketCode() {
        return "CC-" + randomCode(6) + '-' + randomCode(6);
    }

    private String randomCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(CODE_CHARS[secureRandom.nextInt(CODE_CHARS.length)]);
        }
        return builder.toString();
    }
}

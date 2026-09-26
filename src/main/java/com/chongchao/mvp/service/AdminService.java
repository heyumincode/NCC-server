package com.chongchao.mvp.service;

import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.domain.MatchEvent;
import com.chongchao.mvp.domain.NewsArticle;
import com.chongchao.mvp.domain.TicketStats;
import com.chongchao.mvp.domain.TicketExportRow;
import com.chongchao.mvp.dto.AdminMatchRequest;
import com.chongchao.mvp.dto.AdminMatchStatsResponse;
import com.chongchao.mvp.dto.AdminNewsRequest;
import com.chongchao.mvp.repository.MatchEventRepository;
import com.chongchao.mvp.repository.NewsArticleRepository;
import com.chongchao.mvp.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class AdminService {

    private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private final MatchEventRepository matchEventRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final TicketRepository ticketRepository;

    public AdminService(
            MatchEventRepository matchEventRepository,
            NewsArticleRepository newsArticleRepository,
            TicketRepository ticketRepository
    ) {
        this.matchEventRepository = matchEventRepository;
        this.newsArticleRepository = newsArticleRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<MatchEvent> matches() {
        return matchEventRepository.findAllForAdmin();
    }

    @Transactional
    public long createMatch(AdminMatchRequest request) {
        AdminMatchRequest normalized = normalize(request);
        validateMatch(normalized);
        return matchEventRepository.create(normalized);
    }

    @Transactional
    public void updateMatch(long id, AdminMatchRequest request) {
        AdminMatchRequest normalized = normalize(request);
        validateMatch(normalized);
        if (!matchEventRepository.update(id, normalized)) {
            throw new BusinessException(
                    "MATCH_UPDATE_FAILED",
                    "赛事不存在，或新容量小于已预约人数",
                    HttpStatus.CONFLICT
            );
        }
    }

    public AdminMatchStatsResponse stats(long matchEventId) {
        MatchEvent match = matchEventRepository.findById(matchEventId)
                .orElseThrow(() -> new BusinessException("MATCH_NOT_FOUND", "赛事不存在", HttpStatus.NOT_FOUND));
        TicketStats stats = ticketRepository.countByMatch(matchEventId);
        return new AdminMatchStatsResponse(
                match.id(),
                match.capacity(),
                match.remainingCount(),
                stats.reserved(),
                stats.verified(),
                stats.cancelled()
        );
    }

    public byte[] exportTickets(long matchEventId) {
        matchEventRepository.findById(matchEventId)
                .orElseThrow(() -> new BusinessException("MATCH_NOT_FOUND", "赛事不存在", HttpStatus.NOT_FOUND));
        StringBuilder csv = new StringBuilder("\uFEFF订单号,观赛人,手机号,票状态,票码,预约时间,核销时间,核销入口,核销员\r\n");
        for (TicketExportRow row : ticketRepository.findExportRowsByMatch(matchEventId)) {
            csv.append(csvCell(row.orderNo())).append(',')
                    .append(csvCell(row.attendeeName())).append(',')
                    .append(csvCell(row.attendeePhone())).append(',')
                    .append(csvCell(row.status().name())).append(',')
                    .append(csvCell(row.ticketCode())).append(',')
                    .append(csvCell(formatTime(row.reservedAt()))).append(',')
                    .append(csvCell(formatTime(row.verifiedAt()))).append(',')
                    .append(csvCell(row.verifiedGate())).append(',')
                    .append(csvCell(row.verifiedBy())).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public List<NewsArticle> news() {
        return newsArticleRepository.findAllForAdmin();
    }

    @Transactional
    public long createNews(AdminNewsRequest request) {
        return newsArticleRepository.create(normalize(request));
    }

    @Transactional
    public void updateNews(long id, AdminNewsRequest request) {
        if (!newsArticleRepository.update(id, normalize(request))) {
            throw new BusinessException("NEWS_NOT_FOUND", "资讯不存在", HttpStatus.NOT_FOUND);
        }
    }

    private void validateMatch(AdminMatchRequest request) {
        if (!request.bookingEndAt().isAfter(request.bookingStartAt())) {
            throw new BusinessException("INVALID_BOOKING_WINDOW", "预约结束时间必须晚于开始时间", HttpStatus.BAD_REQUEST);
        }
        if (!request.kickoffAt().isAfter(request.bookingStartAt())) {
            throw new BusinessException("INVALID_KICKOFF_TIME", "开球时间必须晚于预约开始时间", HttpStatus.BAD_REQUEST);
        }
    }

    private AdminMatchRequest normalize(AdminMatchRequest request) {
        return new AdminMatchRequest(
                request.competitionName().trim(),
                defaultString(request.roundName()),
                request.homeTeam().trim(),
                request.awayTeam().trim(),
                request.venue().trim(),
                defaultString(request.venueAddress()),
                request.kickoffAt(),
                request.admissionAt(),
                request.bookingStartAt(),
                request.bookingEndAt(),
                request.capacity(),
                request.status(),
                defaultString(request.coverImageUrl()),
                defaultString(request.notice())
        );
    }

    private AdminNewsRequest normalize(AdminNewsRequest request) {
        return new AdminNewsRequest(
                request.title().trim(),
                defaultString(request.summary()),
                defaultString(request.coverImageUrl()),
                request.content().trim(),
                request.status(),
                request.publishedAt()
        );
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatTime(java.time.Instant instant) {
        return instant == null ? "" : CSV_TIME_FORMAT.format(instant);
    }

    private String csvCell(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n") || safeValue.contains("\r")) {
            return '"' + safeValue.replace("\"", "\"\"") + '"';
        }
        return safeValue;
    }
}

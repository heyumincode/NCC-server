package com.chongchao.mvp.repository;

import com.chongchao.mvp.domain.MatchEvent;
import com.chongchao.mvp.domain.MatchStatus;
import com.chongchao.mvp.dto.AdminMatchRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class MatchEventRepository {

    private static final String SELECT_COLUMNS = """
            SELECT id, competition_name, round_name, home_team, away_team,
                   venue, venue_address, kickoff_at, admission_at,
                   booking_start_at, booking_end_at, capacity, reserved_count,
                   status, cover_image_url, notice
            FROM match_event
            """;

    private final JdbcClient jdbcClient;

    public MatchEventRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<MatchEvent> findPublished() {
        return jdbcClient.sql(SELECT_COLUMNS + """
                        WHERE status IN ('PUBLISHED', 'SOLD_OUT')
                        ORDER BY kickoff_at ASC
                        """)
                .query(rowMapper())
                .list();
    }

    public List<MatchEvent> findAllForAdmin() {
        return jdbcClient.sql(SELECT_COLUMNS + " ORDER BY kickoff_at DESC")
                .query(rowMapper())
                .list();
    }

    public Optional<MatchEvent> findPublishedById(long id) {
        return jdbcClient.sql(SELECT_COLUMNS + """
                        WHERE id = :id AND status IN ('PUBLISHED', 'SOLD_OUT')
                        """)
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    public Optional<MatchEvent> findByIdForUpdate(long id) {
        return jdbcClient.sql(SELECT_COLUMNS + " WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    public long create(AdminMatchRequest request) {
        return jdbcClient.sql("""
                        INSERT INTO match_event (
                            competition_name, round_name, home_team, away_team,
                            venue, venue_address, kickoff_at, admission_at,
                            booking_start_at, booking_end_at, capacity, status,
                            cover_image_url, notice
                        ) VALUES (
                            :competitionName, :roundName, :homeTeam, :awayTeam,
                            :venue, :venueAddress, :kickoffAt, :admissionAt,
                            :bookingStartAt, :bookingEndAt, :capacity, :status,
                            :coverImageUrl, :notice
                        )
                        RETURNING id
                        """)
                .param("competitionName", request.competitionName())
                .param("roundName", request.roundName())
                .param("homeTeam", request.homeTeam())
                .param("awayTeam", request.awayTeam())
                .param("venue", request.venue())
                .param("venueAddress", request.venueAddress())
                .param("kickoffAt", java.sql.Timestamp.from(request.kickoffAt()))
                .param("admissionAt", nullableTimestamp(request.admissionAt()), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .param("bookingStartAt", java.sql.Timestamp.from(request.bookingStartAt()))
                .param("bookingEndAt", java.sql.Timestamp.from(request.bookingEndAt()))
                .param("capacity", request.capacity())
                .param("status", request.status().name())
                .param("coverImageUrl", request.coverImageUrl())
                .param("notice", request.notice())
                .query(Long.class)
                .single();
    }

    public boolean update(long id, AdminMatchRequest request) {
        int updated = jdbcClient.sql("""
                        UPDATE match_event
                        SET competition_name = :competitionName,
                            round_name = :roundName,
                            home_team = :homeTeam,
                            away_team = :awayTeam,
                            venue = :venue,
                            venue_address = :venueAddress,
                            kickoff_at = :kickoffAt,
                            admission_at = :admissionAt,
                            booking_start_at = :bookingStartAt,
                            booking_end_at = :bookingEndAt,
                            capacity = :capacity,
                            status = :status,
                            cover_image_url = :coverImageUrl,
                            notice = :notice,
                            updated_at = CURRENT_TIMESTAMP,
                            version = version + 1
                        WHERE id = :id AND :capacity >= reserved_count
                        """)
                .param("id", id)
                .param("competitionName", request.competitionName())
                .param("roundName", request.roundName())
                .param("homeTeam", request.homeTeam())
                .param("awayTeam", request.awayTeam())
                .param("venue", request.venue())
                .param("venueAddress", request.venueAddress())
                .param("kickoffAt", java.sql.Timestamp.from(request.kickoffAt()))
                .param("admissionAt", nullableTimestamp(request.admissionAt()), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .param("bookingStartAt", java.sql.Timestamp.from(request.bookingStartAt()))
                .param("bookingEndAt", java.sql.Timestamp.from(request.bookingEndAt()))
                .param("capacity", request.capacity())
                .param("status", request.status().name())
                .param("coverImageUrl", request.coverImageUrl())
                .param("notice", request.notice())
                .update();
        return updated == 1;
    }

    public void incrementReservedCount(long id) {
        jdbcClient.sql("""
                        UPDATE match_event
                        SET reserved_count = reserved_count + 1,
                            status = CASE WHEN reserved_count + 1 >= capacity THEN 'SOLD_OUT' ELSE status END,
                            updated_at = CURRENT_TIMESTAMP,
                            version = version + 1
                        WHERE id = :id AND reserved_count < capacity
                        """)
                .param("id", id)
                .update();
    }

    public Optional<MatchEvent> findById(long id) {
        return jdbcClient.sql(SELECT_COLUMNS + " WHERE id = :id")
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    private RowMapper<MatchEvent> rowMapper() {
        return (resultSet, rowNum) -> {
            Timestamp admissionTimestamp = resultSet.getTimestamp("admission_at");
            Instant admissionAt = admissionTimestamp == null ? null : admissionTimestamp.toInstant();
            return new MatchEvent(
                    resultSet.getLong("id"),
                    resultSet.getString("competition_name"),
                    resultSet.getString("round_name"),
                    resultSet.getString("home_team"),
                    resultSet.getString("away_team"),
                    resultSet.getString("venue"),
                    resultSet.getString("venue_address"),
                    resultSet.getTimestamp("kickoff_at").toInstant(),
                    admissionAt,
                    resultSet.getTimestamp("booking_start_at").toInstant(),
                    resultSet.getTimestamp("booking_end_at").toInstant(),
                    resultSet.getInt("capacity"),
                    resultSet.getInt("reserved_count"),
                    MatchStatus.valueOf(resultSet.getString("status")),
                    resultSet.getString("cover_image_url"),
                    resultSet.getString("notice")
            );
        };
    }

    private Timestamp nullableTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}

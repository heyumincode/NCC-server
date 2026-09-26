package com.chongchao.mvp.repository;

import com.chongchao.mvp.domain.TicketReservation;
import com.chongchao.mvp.domain.TicketExportRow;
import com.chongchao.mvp.domain.TicketStats;
import com.chongchao.mvp.domain.TicketStatus;
import com.chongchao.mvp.domain.TicketView;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class TicketRepository {

    private static final String TICKET_COLUMNS = """
            SELECT id, order_no, match_event_id, app_user_id, attendee_name,
                   attendee_phone, status, ticket_code, reserved_at, cancelled_at,
                   verified_at, verified_gate, verified_by
            FROM ticket_reservation
            """;

    private static final String TICKET_VIEW_COLUMNS = """
            SELECT t.id, t.order_no, t.match_event_id,
                   m.competition_name, m.round_name, m.home_team, m.away_team,
                   m.venue, m.venue_address, m.kickoff_at, m.admission_at,
                   t.attendee_name, t.attendee_phone, t.status, t.ticket_code,
                   t.reserved_at, t.verified_at, t.verified_gate, t.verified_by
            FROM ticket_reservation t
            JOIN match_event m ON m.id = t.match_event_id
            """;

    private final JdbcClient jdbcClient;

    public TicketRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<TicketReservation> findActiveByUserAndMatch(long appUserId, long matchEventId) {
        return jdbcClient.sql(TICKET_COLUMNS + """
                        WHERE app_user_id = :appUserId
                          AND match_event_id = :matchEventId
                          AND status <> 'CANCELLED'
                        """)
                .param("appUserId", appUserId)
                .param("matchEventId", matchEventId)
                .query(ticketRowMapper())
                .optional();
    }

    public TicketReservation create(
            String orderNo,
            long matchEventId,
            long appUserId,
            String attendeeName,
            String attendeePhone,
            String ticketCode
    ) {
        return jdbcClient.sql("""
                        INSERT INTO ticket_reservation (
                            order_no, match_event_id, app_user_id, attendee_name,
                            attendee_phone, ticket_code
                        ) VALUES (
                            :orderNo, :matchEventId, :appUserId, :attendeeName,
                            :attendeePhone, :ticketCode
                        )
                        RETURNING id, order_no, match_event_id, app_user_id, attendee_name,
                                  attendee_phone, status, ticket_code, reserved_at, cancelled_at,
                                  verified_at, verified_gate, verified_by
                        """)
                .param("orderNo", orderNo)
                .param("matchEventId", matchEventId)
                .param("appUserId", appUserId)
                .param("attendeeName", attendeeName)
                .param("attendeePhone", attendeePhone)
                .param("ticketCode", ticketCode)
                .query(ticketRowMapper())
                .single();
    }

    public List<TicketView> findViewsByUser(long appUserId) {
        return jdbcClient.sql(TICKET_VIEW_COLUMNS + """
                        WHERE t.app_user_id = :appUserId
                          AND t.status <> 'CANCELLED'
                        ORDER BY m.kickoff_at DESC
                        """)
                .param("appUserId", appUserId)
                .query(ticketViewRowMapper())
                .list();
    }

    public Optional<TicketView> findViewByIdAndUser(long id, long appUserId) {
        return jdbcClient.sql(TICKET_VIEW_COLUMNS + """
                        WHERE t.id = :id AND t.app_user_id = :appUserId
                        """)
                .param("id", id)
                .param("appUserId", appUserId)
                .query(ticketViewRowMapper())
                .optional();
    }

    public Optional<TicketView> findViewByTicketCode(String ticketCode) {
        return jdbcClient.sql(TICKET_VIEW_COLUMNS + " WHERE t.ticket_code = :ticketCode")
                .param("ticketCode", ticketCode)
                .query(ticketViewRowMapper())
                .optional();
    }

    public Optional<TicketReservation> findByTicketCodeForUpdate(String ticketCode) {
        return jdbcClient.sql(TICKET_COLUMNS + " WHERE ticket_code = :ticketCode FOR UPDATE")
                .param("ticketCode", ticketCode)
                .query(ticketRowMapper())
                .optional();
    }

    public void markVerified(long id, String gateName, String staffName) {
        jdbcClient.sql("""
                        UPDATE ticket_reservation
                        SET status = 'VERIFIED',
                            verified_at = CURRENT_TIMESTAMP,
                            verified_gate = :gateName,
                            verified_by = :staffName,
                            updated_at = CURRENT_TIMESTAMP,
                            version = version + 1
                        WHERE id = :id AND status = 'RESERVED'
                        """)
                .param("id", id)
                .param("gateName", gateName)
                .param("staffName", staffName)
                .update();
    }

    public TicketStats countByMatch(long matchEventId) {
        return jdbcClient.sql("""
                        SELECT
                            COUNT(*) FILTER (WHERE status = 'RESERVED') AS reserved,
                            COUNT(*) FILTER (WHERE status = 'VERIFIED') AS verified,
                            COUNT(*) FILTER (WHERE status = 'CANCELLED') AS cancelled
                        FROM ticket_reservation
                        WHERE match_event_id = :matchEventId
                        """)
                .param("matchEventId", matchEventId)
                .query((resultSet, rowNum) -> new TicketStats(
                        resultSet.getLong("reserved"),
                        resultSet.getLong("verified"),
                        resultSet.getLong("cancelled")
                ))
                .single();
    }

    public List<TicketExportRow> findExportRowsByMatch(long matchEventId) {
        return jdbcClient.sql("""
                        SELECT order_no, attendee_name, attendee_phone, status, ticket_code,
                               reserved_at, verified_at, verified_gate, verified_by
                        FROM ticket_reservation
                        WHERE match_event_id = :matchEventId
                        ORDER BY reserved_at ASC
                        """)
                .param("matchEventId", matchEventId)
                .query((resultSet, rowNum) -> new TicketExportRow(
                        resultSet.getString("order_no"),
                        resultSet.getString("attendee_name"),
                        resultSet.getString("attendee_phone"),
                        TicketStatus.valueOf(resultSet.getString("status")),
                        resultSet.getString("ticket_code"),
                        resultSet.getTimestamp("reserved_at").toInstant(),
                        nullableInstant(resultSet.getTimestamp("verified_at")),
                        resultSet.getString("verified_gate"),
                        resultSet.getString("verified_by")
                ))
                .list();
    }

    private RowMapper<TicketReservation> ticketRowMapper() {
        return (resultSet, rowNum) -> new TicketReservation(
                resultSet.getLong("id"),
                resultSet.getString("order_no"),
                resultSet.getLong("match_event_id"),
                resultSet.getLong("app_user_id"),
                resultSet.getString("attendee_name"),
                resultSet.getString("attendee_phone"),
                TicketStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("ticket_code"),
                resultSet.getTimestamp("reserved_at").toInstant(),
                nullableInstant(resultSet.getTimestamp("cancelled_at")),
                nullableInstant(resultSet.getTimestamp("verified_at")),
                resultSet.getString("verified_gate"),
                resultSet.getString("verified_by")
        );
    }

    private RowMapper<TicketView> ticketViewRowMapper() {
        return (resultSet, rowNum) -> new TicketView(
                resultSet.getLong("id"),
                resultSet.getString("order_no"),
                resultSet.getLong("match_event_id"),
                resultSet.getString("competition_name"),
                resultSet.getString("round_name"),
                resultSet.getString("home_team"),
                resultSet.getString("away_team"),
                resultSet.getString("venue"),
                resultSet.getString("venue_address"),
                resultSet.getTimestamp("kickoff_at").toInstant(),
                nullableInstant(resultSet.getTimestamp("admission_at")),
                resultSet.getString("attendee_name"),
                resultSet.getString("attendee_phone"),
                TicketStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("ticket_code"),
                resultSet.getTimestamp("reserved_at").toInstant(),
                nullableInstant(resultSet.getTimestamp("verified_at")),
                resultSet.getString("verified_gate"),
                resultSet.getString("verified_by")
        );
    }

    private Instant nullableInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}

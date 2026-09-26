package com.chongchao.mvp.repository;

import com.chongchao.mvp.domain.VerificationResult;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class VerificationLogRepository {

    private final JdbcClient jdbcClient;

    public VerificationLogRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void create(
            Long ticketReservationId,
            String ticketCode,
            VerificationResult result,
            String gateName,
            String staffName,
            String detail
    ) {
        jdbcClient.sql("""
                        INSERT INTO ticket_verification_log (
                            ticket_reservation_id, ticket_code, result,
                            gate_name, staff_name, detail
                        ) VALUES (
                            :ticketReservationId, :ticketCode, :result,
                            :gateName, :staffName, :detail
                        )
                        """)
                .param("ticketReservationId", ticketReservationId, java.sql.Types.BIGINT)
                .param("ticketCode", ticketCode)
                .param("result", result.name())
                .param("gateName", gateName)
                .param("staffName", staffName)
                .param("detail", detail)
                .update();
    }
}

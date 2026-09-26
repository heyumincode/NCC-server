package com.chongchao.mvp.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class StaffSessionRepository {

    private final JdbcClient jdbcClient;

    public StaffSessionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void create(String staffName, String tokenHash, Instant expiresAt) {
        jdbcClient.sql("""
                        INSERT INTO staff_session (staff_name, token_hash, expires_at)
                        VALUES (:staffName, :tokenHash, :expiresAt)
                        """)
                .param("staffName", staffName)
                .param("tokenHash", tokenHash)
                .param("expiresAt", java.sql.Timestamp.from(expiresAt))
                .update();
    }

    public Optional<String> findActiveStaffName(String tokenHash) {
        return jdbcClient.sql("""
                        SELECT staff_name
                        FROM staff_session
                        WHERE token_hash = :tokenHash
                          AND expires_at > CURRENT_TIMESTAMP
                        """)
                .param("tokenHash", tokenHash)
                .query(String.class)
                .optional();
    }

    public void touch(String tokenHash) {
        jdbcClient.sql("""
                        UPDATE staff_session
                        SET last_used_at = CURRENT_TIMESTAMP
                        WHERE token_hash = :tokenHash
                        """)
                .param("tokenHash", tokenHash)
                .update();
    }
}

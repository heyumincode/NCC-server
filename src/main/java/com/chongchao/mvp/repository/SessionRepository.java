package com.chongchao.mvp.repository;

import com.chongchao.mvp.domain.AppUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class SessionRepository {

    private final JdbcClient jdbcClient;

    public SessionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void create(long appUserId, String tokenHash, Instant expiresAt) {
        jdbcClient.sql("""
                        INSERT INTO user_session (app_user_id, token_hash, expires_at)
                        VALUES (:appUserId, :tokenHash, :expiresAt)
                        """)
                .param("appUserId", appUserId)
                .param("tokenHash", tokenHash)
                .param("expiresAt", java.sql.Timestamp.from(expiresAt))
                .update();
    }

    public Optional<AppUser> findActiveUser(String tokenHash) {
        return jdbcClient.sql("""
                        SELECT u.id, u.wechat_open_id, u.nickname, u.avatar_url, u.is_active
                        FROM user_session s
                        JOIN app_user u ON u.id = s.app_user_id
                        WHERE s.token_hash = :tokenHash
                          AND s.expires_at > CURRENT_TIMESTAMP
                          AND u.is_active = true
                        """)
                .param("tokenHash", tokenHash)
                .query((resultSet, rowNum) -> new AppUser(
                        resultSet.getLong("id"),
                        resultSet.getString("wechat_open_id"),
                        resultSet.getString("nickname"),
                        resultSet.getString("avatar_url"),
                        resultSet.getBoolean("is_active")
                ))
                .optional();
    }

    public void touch(String tokenHash) {
        jdbcClient.sql("""
                        UPDATE user_session
                        SET last_used_at = CURRENT_TIMESTAMP
                        WHERE token_hash = :tokenHash
                        """)
                .param("tokenHash", tokenHash)
                .update();
    }
}

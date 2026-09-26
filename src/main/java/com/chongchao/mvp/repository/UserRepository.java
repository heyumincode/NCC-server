package com.chongchao.mvp.repository;

import com.chongchao.mvp.domain.AppUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public AppUser upsert(String openId, String nickname, String avatarUrl) {
        return jdbcClient.sql("""
                        INSERT INTO app_user (wechat_open_id, nickname, avatar_url)
                        VALUES (:openId, :nickname, :avatarUrl)
                        ON CONFLICT (wechat_open_id) DO UPDATE
                        SET nickname = EXCLUDED.nickname,
                            avatar_url = EXCLUDED.avatar_url,
                            updated_at = CURRENT_TIMESTAMP,
                            version = app_user.version + 1
                        RETURNING id, wechat_open_id, nickname, avatar_url, is_active
                        """)
                .param("openId", openId)
                .param("nickname", nickname)
                .param("avatarUrl", avatarUrl)
                .query((resultSet, rowNum) -> new AppUser(
                        resultSet.getLong("id"),
                        resultSet.getString("wechat_open_id"),
                        resultSet.getString("nickname"),
                        resultSet.getString("avatar_url"),
                        resultSet.getBoolean("is_active")
                ))
                .single();
    }
}


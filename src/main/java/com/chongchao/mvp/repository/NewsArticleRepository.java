package com.chongchao.mvp.repository;

import com.chongchao.mvp.domain.ContentStatus;
import com.chongchao.mvp.domain.NewsArticle;
import com.chongchao.mvp.dto.AdminNewsRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class NewsArticleRepository {

    private static final String SELECT_COLUMNS = """
            SELECT id, title, summary, cover_image_url, content, status, published_at
            FROM news_article
            """;

    private final JdbcClient jdbcClient;

    public NewsArticleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<NewsArticle> findPublished(int limit) {
        return jdbcClient.sql(SELECT_COLUMNS + """
                        WHERE status = 'PUBLISHED'
                        ORDER BY published_at DESC
                        LIMIT :limit
                        """)
                .param("limit", limit)
                .query(rowMapper())
                .list();
    }

    public Optional<NewsArticle> findPublishedById(long id) {
        return jdbcClient.sql(SELECT_COLUMNS + " WHERE id = :id AND status = 'PUBLISHED'")
                .param("id", id)
                .query(rowMapper())
                .optional();
    }

    public List<NewsArticle> findAllForAdmin() {
        return jdbcClient.sql(SELECT_COLUMNS + " ORDER BY created_at DESC")
                .query(rowMapper())
                .list();
    }

    public long create(AdminNewsRequest request) {
        Instant publishedAt = request.status() == ContentStatus.PUBLISHED ? Instant.now() : request.publishedAt();
        return jdbcClient.sql("""
                        INSERT INTO news_article (
                            title, summary, cover_image_url, content, status, published_at
                        ) VALUES (
                            :title, :summary, :coverImageUrl, :content, :status, :publishedAt
                        )
                        RETURNING id
                        """)
                .param("title", request.title())
                .param("summary", request.summary())
                .param("coverImageUrl", request.coverImageUrl())
                .param("content", request.content())
                .param("status", request.status().name())
                .param("publishedAt", nullableTimestamp(publishedAt), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .query(Long.class)
                .single();
    }

    public boolean update(long id, AdminNewsRequest request) {
        Instant publishedAt = request.status() == ContentStatus.PUBLISHED
                ? (request.publishedAt() == null ? Instant.now() : request.publishedAt())
                : request.publishedAt();
        int updated = jdbcClient.sql("""
                        UPDATE news_article
                        SET title = :title,
                            summary = :summary,
                            cover_image_url = :coverImageUrl,
                            content = :content,
                            status = :status,
                            published_at = :publishedAt,
                            updated_at = CURRENT_TIMESTAMP,
                            version = version + 1
                        WHERE id = :id
                        """)
                .param("id", id)
                .param("title", request.title())
                .param("summary", request.summary())
                .param("coverImageUrl", request.coverImageUrl())
                .param("content", request.content())
                .param("status", request.status().name())
                .param("publishedAt", nullableTimestamp(publishedAt), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .update();
        return updated == 1;
    }

    private RowMapper<NewsArticle> rowMapper() {
        return (resultSet, rowNum) -> {
            Timestamp publishedTimestamp = resultSet.getTimestamp("published_at");
            return new NewsArticle(
                    resultSet.getLong("id"),
                    resultSet.getString("title"),
                    resultSet.getString("summary"),
                    resultSet.getString("cover_image_url"),
                    resultSet.getString("content"),
                    ContentStatus.valueOf(resultSet.getString("status")),
                    publishedTimestamp == null ? null : publishedTimestamp.toInstant()
            );
        };
    }

    private Timestamp nullableTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}

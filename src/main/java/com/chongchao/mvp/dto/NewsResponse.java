package com.chongchao.mvp.dto;

import com.chongchao.mvp.domain.NewsArticle;

import java.time.Instant;

public record NewsResponse(
        long id,
        String title,
        String summary,
        String coverImageUrl,
        String content,
        Instant publishedAt
) {
    public static NewsResponse from(NewsArticle article, boolean includeContent) {
        return new NewsResponse(
                article.id(),
                article.title(),
                article.summary(),
                article.coverImageUrl(),
                includeContent ? article.content() : null,
                article.publishedAt()
        );
    }
}


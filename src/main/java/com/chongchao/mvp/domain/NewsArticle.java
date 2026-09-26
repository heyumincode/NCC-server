package com.chongchao.mvp.domain;

import java.time.Instant;

public record NewsArticle(
        long id,
        String title,
        String summary,
        String coverImageUrl,
        String content,
        ContentStatus status,
        Instant publishedAt
) {
}


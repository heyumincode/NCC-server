package com.chongchao.mvp.dto;

import com.chongchao.mvp.domain.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AdminNewsRequest(
        @NotBlank(message = "标题不能为空") @Size(max = 120) String title,
        @Size(max = 300) String summary,
        String coverImageUrl,
        @NotBlank(message = "正文不能为空") String content,
        @NotNull(message = "状态不能为空") ContentStatus status,
        Instant publishedAt
) {
}


package com.chongchao.mvp.dto;

import com.chongchao.mvp.domain.MatchStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record AdminMatchRequest(
        @NotBlank(message = "赛事名称不能为空") @Size(max = 80) String competitionName,
        @Size(max = 40) String roundName,
        @NotBlank(message = "主队不能为空") @Size(max = 80) String homeTeam,
        @NotBlank(message = "客队不能为空") @Size(max = 80) String awayTeam,
        @NotBlank(message = "场馆不能为空") @Size(max = 120) String venue,
        @Size(max = 200) String venueAddress,
        @NotNull(message = "开球时间不能为空") Instant kickoffAt,
        Instant admissionAt,
        @NotNull(message = "预约开始时间不能为空") Instant bookingStartAt,
        @NotNull(message = "预约结束时间不能为空") Instant bookingEndAt,
        @Min(value = 1, message = "开放票量必须大于 0") int capacity,
        @NotNull(message = "状态不能为空") MatchStatus status,
        String coverImageUrl,
        String notice
) {
}


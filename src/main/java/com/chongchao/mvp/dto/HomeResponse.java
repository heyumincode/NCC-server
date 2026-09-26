package com.chongchao.mvp.dto;

import java.util.List;

public record HomeResponse(
        Brand brand,
        MatchResponse nextMatch,
        List<MatchResponse> matches,
        List<NewsResponse> news
) {
    public record Brand(
            String name,
            String slogan,
            String regionLabel,
            String primaryColor,
            String servicePhone
    ) {
    }
}


package com.chongchao.mvp.service;

import com.chongchao.mvp.common.BusinessException;
import com.chongchao.mvp.dto.HomeResponse;
import com.chongchao.mvp.dto.MatchResponse;
import com.chongchao.mvp.dto.NewsResponse;
import com.chongchao.mvp.repository.MatchEventRepository;
import com.chongchao.mvp.repository.NewsArticleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicContentService {

    private final MatchEventRepository matchEventRepository;
    private final NewsArticleRepository newsArticleRepository;

    public PublicContentService(
            MatchEventRepository matchEventRepository,
            NewsArticleRepository newsArticleRepository
    ) {
        this.matchEventRepository = matchEventRepository;
        this.newsArticleRepository = newsArticleRepository;
    }

    public HomeResponse home() {
        List<MatchResponse> matches = matchEventRepository.findPublished().stream()
                .map(MatchResponse::from)
                .toList();
        List<NewsResponse> news = newsArticleRepository.findPublished(4).stream()
                .map(article -> NewsResponse.from(article, false))
                .toList();
        MatchResponse nextMatch = matches.isEmpty() ? null : matches.get(0);
        HomeResponse.Brand brand = new HomeResponse.Brand(
                "充超联赛",
                "为热爱上场，为城市而战",
                "南充九县市区足球联赛",
                "#D20A2E",
                ""
        );
        return new HomeResponse(brand, nextMatch, matches, news);
    }

    public List<MatchResponse> matches() {
        return matchEventRepository.findPublished().stream().map(MatchResponse::from).toList();
    }

    public MatchResponse match(long id) {
        return matchEventRepository.findPublishedById(id)
                .map(MatchResponse::from)
                .orElseThrow(() -> new BusinessException("MATCH_NOT_FOUND", "赛事不存在或尚未发布", HttpStatus.NOT_FOUND));
    }

    public List<NewsResponse> news() {
        return newsArticleRepository.findPublished(50).stream()
                .map(article -> NewsResponse.from(article, false))
                .toList();
    }

    public NewsResponse news(long id) {
        return newsArticleRepository.findPublishedById(id)
                .map(article -> NewsResponse.from(article, true))
                .orElseThrow(() -> new BusinessException("NEWS_NOT_FOUND", "资讯不存在或尚未发布", HttpStatus.NOT_FOUND));
    }
}



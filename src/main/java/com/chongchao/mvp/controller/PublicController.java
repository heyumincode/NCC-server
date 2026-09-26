package com.chongchao.mvp.controller;

import com.chongchao.mvp.common.ApiResponse;
import com.chongchao.mvp.dto.HomeResponse;
import com.chongchao.mvp.dto.MatchResponse;
import com.chongchao.mvp.dto.NewsResponse;
import com.chongchao.mvp.service.PublicContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final PublicContentService publicContentService;

    public PublicController(PublicContentService publicContentService) {
        this.publicContentService = publicContentService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeResponse> home() {
        return ApiResponse.ok(publicContentService.home());
    }

    @GetMapping("/matches")
    public ApiResponse<List<MatchResponse>> matches() {
        return ApiResponse.ok(publicContentService.matches());
    }

    @GetMapping("/matches/{id}")
    public ApiResponse<MatchResponse> match(@PathVariable long id) {
        return ApiResponse.ok(publicContentService.match(id));
    }

    @GetMapping("/news")
    public ApiResponse<List<NewsResponse>> news() {
        return ApiResponse.ok(publicContentService.news());
    }

    @GetMapping("/news/{id}")
    public ApiResponse<NewsResponse> news(@PathVariable long id) {
        return ApiResponse.ok(publicContentService.news(id));
    }
}


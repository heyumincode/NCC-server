package com.chongchao.mvp.controller;

import com.chongchao.mvp.common.ApiResponse;
import com.chongchao.mvp.domain.MatchEvent;
import com.chongchao.mvp.domain.NewsArticle;
import com.chongchao.mvp.dto.AdminMatchRequest;
import com.chongchao.mvp.dto.AdminMatchStatsResponse;
import com.chongchao.mvp.dto.AdminNewsRequest;
import com.chongchao.mvp.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/matches")
    public ApiResponse<List<MatchEvent>> matches() {
        return ApiResponse.ok(adminService.matches());
    }

    @PostMapping("/matches")
    public ApiResponse<Map<String, Long>> createMatch(@Valid @RequestBody AdminMatchRequest request) {
        return ApiResponse.ok(Map.of("id", adminService.createMatch(request)));
    }

    @PutMapping("/matches/{id}")
    public ApiResponse<Void> updateMatch(@PathVariable long id, @Valid @RequestBody AdminMatchRequest request) {
        adminService.updateMatch(id, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/matches/{id}/stats")
    public ApiResponse<AdminMatchStatsResponse> matchStats(@PathVariable long id) {
        return ApiResponse.ok(adminService.stats(id));
    }

    @GetMapping(value = "/matches/{id}/tickets.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportTickets(@PathVariable long id) {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=match-" + id + "-tickets.csv")
                .body(adminService.exportTickets(id));
    }

    @GetMapping("/news")
    public ApiResponse<List<NewsArticle>> news() {
        return ApiResponse.ok(adminService.news());
    }

    @PostMapping("/news")
    public ApiResponse<Map<String, Long>> createNews(@Valid @RequestBody AdminNewsRequest request) {
        return ApiResponse.ok(Map.of("id", adminService.createNews(request)));
    }

    @PutMapping("/news/{id}")
    public ApiResponse<Void> updateNews(@PathVariable long id, @Valid @RequestBody AdminNewsRequest request) {
        adminService.updateNews(id, request);
        return ApiResponse.ok(null);
    }
}

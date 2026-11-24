package com.tadak.akai.controller;

import com.tadak.akai.dto.QuestionRequest;
import com.tadak.akai.dto.SearchComparisonView;
import com.tadak.akai.dto.StatisticsView;
import com.tadak.akai.service.SearchComparisonService;
import com.tadak.akai.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 전용 화면의 API. /api/admin/** 는 SecurityConfig에서 관리자만 허용한다. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SearchComparisonService searchComparisonService;
    private final StatisticsService statisticsService;

    public AdminController(SearchComparisonService searchComparisonService, StatisticsService statisticsService) {
        this.searchComparisonService = searchComparisonService;
        this.statisticsService = statisticsService;
    }

    /** F-09: 품질 지표 대시보드 */
    @GetMapping("/statistics")
    public StatisticsView statistics() {
        return statisticsService.statistics();
    }

    /** F-10: 키워드 검색과 임베딩 검색 결과 비교 */
    @PostMapping("/search-comparison")
    public SearchComparisonView compareSearch(@RequestBody QuestionRequest request) {
        return searchComparisonService.compare(request.question());
    }
}

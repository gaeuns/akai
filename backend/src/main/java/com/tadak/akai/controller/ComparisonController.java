package com.tadak.akai.controller;

import com.tadak.akai.dto.ComparisonRequest;
import com.tadak.akai.dto.ComparisonView;
import com.tadak.akai.service.ComparisonService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** F-07: 여러 문서 비교 요약. 로그인한 사용자 모두 쓸 수 있다(이용자 기능). */
@RestController
@RequestMapping("/api/comparisons")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping
    public ComparisonView compare(@RequestBody ComparisonRequest request) {
        return comparisonService.compare(request.documentIds());
    }
}

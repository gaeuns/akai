package com.tadak.akai.dto;

import java.util.List;

/**
 * F-07: 비교 요약 결과. AI가 만든 글이라 aiGenerated는 항상 true다.
 * usedSummary가 false인 문서는 요약이 없어 본문 앞부분으로 비교했다.
 */
public record ComparisonView(List<ComparedDocument> documents, String comparison, boolean aiGenerated) {

    public record ComparedDocument(Long id, String title, boolean usedSummary) {
    }
}

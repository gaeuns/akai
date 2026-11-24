package com.tadak.akai.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * F-09: 관리자 품질 지표. 비율은 0~100(%)이고, 질문이 없으면 null이다.
 * notFoundRate는 전체 질문 중 "문서에서 찾을 수 없음" 비율(NF-01 거절 동작 확인용),
 * within5sRate는 5초 이내 응답 비율(NF-02)이다.
 */
public record StatisticsView(
        String searchStrategy,
        long documentCount,
        long chunkCount,
        long embeddedChunkCount,
        long questionCount,
        long notFoundCount,
        Double notFoundRate,
        Double averageResponseSeconds,
        Double within5sRate,
        List<DocumentUsage> topDocuments,
        List<RecentQuestion> recentNotFound) {

    public record DocumentUsage(Long documentId, String title, long evidenceCount) {
    }

    public record RecentQuestion(String question, LocalDateTime askedAt) {
    }
}

package com.tadak.akai.dto;

import com.tadak.akai.search.ScoredChunk;
import java.util.List;

/** F-10: 같은 질문을 키워드 검색과 임베딩 검색으로 찾은 결과 */
public record SearchComparisonView(String question, StrategyResult keyword, StrategyResult embedding) {

    /**
     * 검색 방식 하나의 결과. criterion은 관련 조각으로 보는 기준, error는 검색이 실패했을 때의 안내다.
     * keywords는 키워드 검색이 질문에서 뽑은 키워드(임베딩은 null)다.
     */
    public record StrategyResult(
            String criterion, List<String> keywords, long elapsedMillis, String error, List<ScoredEvidence> results) {
    }

    public record ScoredEvidence(
            Long chunkId, Long documentId, String documentTitle, int pageNo, String content,
            double score, boolean relevant) {

        public static ScoredEvidence from(ScoredChunk scored) {
            return new ScoredEvidence(
                    scored.chunk().getId(),
                    scored.chunk().getDocument().getId(),
                    scored.chunk().getDocument().getTitle(),
                    scored.chunk().getPageNo(),
                    scored.chunk().getContent(),
                    scored.score(),
                    scored.relevant());
        }
    }
}

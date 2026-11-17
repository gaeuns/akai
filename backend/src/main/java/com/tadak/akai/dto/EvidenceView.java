package com.tadak.akai.dto;

import com.tadak.akai.domain.Chunk;

/** 답변 근거 한 건: 문서명·페이지·원문 (AI 기능 규칙 3) */
public record EvidenceView(Long chunkId, Long documentId, String documentTitle, int pageNo, String content) {

    public static EvidenceView from(Chunk chunk) {
        return new EvidenceView(
                chunk.getId(),
                chunk.getDocument().getId(),
                chunk.getDocument().getTitle(),
                chunk.getPageNo(),
                chunk.getContent());
    }
}

package com.tadak.akai.dto;

import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.Summary;
import java.time.LocalDateTime;
import java.util.List;

/** 문서 상세: 요약·키워드와 조각 목록 */
public record DocumentDetail(
        Long id,
        String title,
        String fileName,
        String fileType,
        String status,
        LocalDateTime createdAt,
        String summary,
        String keywords,
        boolean aiGenerated,
        List<ChunkView> chunks) {

    public static DocumentDetail from(Document document) {
        Summary summary = document.getSummary();
        return new DocumentDetail(
                document.getId(),
                document.getTitle(),
                document.getFileName(),
                document.getFileType().name(),
                document.getStatus().name(),
                document.getCreatedAt(),
                summary == null ? null : summary.getContent(),
                summary == null ? null : summary.getKeywords(),
                summary != null && summary.isAiGenerated(),
                document.getChunks().stream().map(ChunkView::from).toList());
    }
}

package com.tadak.akai.dto;

import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.Summary;
import java.time.LocalDateTime;

/** 문서 목록 한 줄 */
public record DocumentListItem(
        Long id,
        String title,
        String fileType,
        String status,
        LocalDateTime createdAt,
        String keywords,
        boolean aiGenerated,
        int chunkCount) {

    public static DocumentListItem from(Document document) {
        Summary summary = document.getSummary();
        return new DocumentListItem(
                document.getId(),
                document.getTitle(),
                document.getFileType().name(),
                document.getStatus().name(),
                document.getCreatedAt(),
                summary == null ? null : summary.getKeywords(),
                summary != null && summary.isAiGenerated(),
                document.getChunks().size());
    }
}

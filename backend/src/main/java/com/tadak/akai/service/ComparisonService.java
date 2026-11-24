package com.tadak.akai.service;

import com.tadak.akai.ai.ComparisonGenerator;
import com.tadak.akai.ai.ComparisonSource;
import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.Summary;
import com.tadak.akai.dto.ComparisonView;
import com.tadak.akai.dto.ComparisonView.ComparedDocument;
import com.tadak.akai.repository.DocumentRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F-07: 고른 문서들의 공통점·차이점을 AI로 요약한다.
 * 업로드 때 저장한 요약을 다시 쓰고(요약 재생성 없음), 요약이 없는 문서만 본문 앞부분을 보낸다.
 * 결과는 저장하지 않고 바로 보여 준다.
 */
@Service
@Transactional(readOnly = true)
public class ComparisonService {

    public static final int MIN_DOCUMENTS = 2;
    public static final int MAX_DOCUMENTS = 5;
    /** 요약이 없는 문서에서 보낼 본문 길이 */
    static final int TEXT_LIMIT = 3000;

    private static final Logger log = LoggerFactory.getLogger(ComparisonService.class);

    private final DocumentRepository documentRepository;
    private final ComparisonGenerator comparisonGenerator;

    public ComparisonService(DocumentRepository documentRepository, ComparisonGenerator comparisonGenerator) {
        this.documentRepository = documentRepository;
        this.comparisonGenerator = comparisonGenerator;
    }

    public ComparisonView compare(List<Long> documentIds) {
        List<Document> documents = validate(documentIds).stream().map(this::getDocument).toList();
        List<ComparisonSource> sources = documents.stream().map(this::toSource).toList();

        long start = System.nanoTime();
        String comparison = comparisonGenerator.compare(sources);
        log.info("비교 요약 완료: 문서 {}개 | {}초", documents.size(),
                String.format("%.1f", (System.nanoTime() - start) / 1_000_000_000.0));

        List<ComparedDocument> compared = documents.stream()
                .map(document -> new ComparedDocument(
                        document.getId(), document.getTitle(), document.getSummary() != null))
                .toList();
        return new ComparisonView(compared, comparison, true);
    }

    private List<Long> validate(List<Long> documentIds) {
        List<Long> ids = documentIds == null ? List.of() : documentIds.stream().distinct().toList();
        if (ids.size() < MIN_DOCUMENTS || ids.contains(null)) {
            throw new IllegalArgumentException("비교할 문서를 " + MIN_DOCUMENTS + "개 이상 고르세요.");
        }
        if (ids.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException("한 번에 " + MAX_DOCUMENTS + "개까지 비교할 수 있습니다.");
        }
        return ids;
    }

    private Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("문서를 찾을 수 없습니다: " + id));
    }

    private ComparisonSource toSource(Document document) {
        Summary summary = document.getSummary();
        if (summary != null) {
            String keywords = summary.getKeywords() == null ? "" : "\n키워드: " + summary.getKeywords();
            return new ComparisonSource(document.getTitle(), summary.getContent() + keywords);
        }
        String text = document.getChunks().stream().map(Chunk::getContent).collect(Collectors.joining("\n"));
        String excerpt = text.length() > TEXT_LIMIT ? text.substring(0, TEXT_LIMIT) : text;
        return new ComparisonSource(document.getTitle(), excerpt);
    }
}

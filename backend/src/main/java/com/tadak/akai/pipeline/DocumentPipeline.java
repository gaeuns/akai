package com.tadak.akai.pipeline;

import com.tadak.akai.ai.SummaryGenerator;
import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.DocumentStatus;
import com.tadak.akai.domain.FileType;
import com.tadak.akai.domain.Summary;
import com.tadak.akai.extractor.PageText;
import com.tadak.akai.extractor.TextExtractor;
import com.tadak.akai.extractor.TextExtractorFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 파이프-필터: 추출 → 분할 → 요약·키워드 순서만 정한다. 각 단계는 별도 클래스가 맡는다. */
@Component
public class DocumentPipeline {

    private static final Logger log = LoggerFactory.getLogger(DocumentPipeline.class);

    /** 요약 요청에 넣을 본문 최대 길이 (너무 긴 문서는 앞부분만 보낸다) */
    private static final int MAX_SUMMARY_INPUT = 30000;

    private final TextExtractorFactory extractorFactory;
    private final ChunkSplitter chunkSplitter;
    private final SummaryGenerator summaryGenerator;

    public DocumentPipeline(
            TextExtractorFactory extractorFactory,
            ChunkSplitter chunkSplitter,
            SummaryGenerator summaryGenerator) {
        this.extractorFactory = extractorFactory;
        this.chunkSplitter = chunkSplitter;
        this.summaryGenerator = summaryGenerator;
    }

    public Document process(Document document, Path file) throws IOException {
        long startedAt = System.currentTimeMillis();

        TextExtractor extractor = extractorFactory.create(document.getFileType());
        List<PageText> pages = extractor.extract(file);
        long extractedAt = System.currentTimeMillis();

        List<Chunk> chunks = chunkSplitter.split(document, pages);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(noTextMessage(document.getFileType()));
        }
        chunks.forEach(document::addChunk);
        long splitAt = System.currentTimeMillis();

        summarize(document, pages);
        document.changeStatus(DocumentStatus.READY);
        long finishedAt = System.currentTimeMillis();

        log.info("문서 처리 완료: 제목={}, 페이지={}, 조각={} | 추출 {}, 분할 {}, 요약 {} | 전체 {} (목표 30초 이내)",
                document.getTitle(), pages.size(), chunks.size(),
                seconds(extractedAt - startedAt), seconds(splitAt - extractedAt),
                seconds(finishedAt - splitAt), seconds(finishedAt - startedAt));
        return document;
    }

    private static String noTextMessage(FileType fileType) {
        String message = "문서에서 텍스트를 찾을 수 없습니다.";
        return fileType == FileType.PDF ? message + " 스캔한 이미지 PDF는 지원하지 않습니다." : message;
    }

    private static String seconds(long millis) {
        return String.format("%.1f초", millis / 1000.0);
    }

    /** 요약에 실패해도 문서 등록은 유지한다. 요약이 없으면 화면에서 "요약 없음"으로 보인다. */
    private void summarize(Document document, List<PageText> pages) {
        String text = pages.stream().map(PageText::text).collect(Collectors.joining("\n"));
        if (text.length() > MAX_SUMMARY_INPUT) {
            text = text.substring(0, MAX_SUMMARY_INPUT);
        }
        try {
            String summary = summaryGenerator.summarize(text);
            List<String> keywords = summaryGenerator.extractKeywords(text);
            document.assignSummary(new Summary(document, summary, String.join(", ", keywords)));
        } catch (RuntimeException e) {
            log.warn("요약 생성 실패, 요약 없이 저장: 제목={}, 원인={}", document.getTitle(), e.getMessage());
        }
    }
}

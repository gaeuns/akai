package com.tadak.akai.pipeline;

import com.tadak.akai.ai.SummaryGenerator;
import com.tadak.akai.domain.Document;
import com.tadak.akai.extractor.TextExtractorFactory;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/** 파이프-필터: 추출 → 분할 → 요약·키워드 순서만 정한다. 각 단계는 별도 클래스가 맡는다. */
@Component
public class DocumentPipeline {

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

    public Document process(Document document, Path file) {
        // TODO(F-01, F-02): 추출 → 분할 → 요약·키워드 생성, 단계별 처리 시간 로그(NF-02)
        throw new UnsupportedOperationException("F-01, F-02 구현 예정");
    }
}

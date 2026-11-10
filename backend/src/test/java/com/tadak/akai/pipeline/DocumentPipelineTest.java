package com.tadak.akai.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tadak.akai.ai.SummaryGenerator;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.DocumentStatus;
import com.tadak.akai.domain.FileType;
import com.tadak.akai.extractor.TextExtractorFactory;
import com.tadak.akai.extractor.TxtTextExtractor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentPipelineTest {

    private final SummaryGenerator failingSummary = mock(SummaryGenerator.class);
    private final DocumentPipeline pipeline = new DocumentPipeline(
            new TextExtractorFactory(List.of(new TxtTextExtractor())), new ChunkSplitter(), failingSummary);

    @Test
    void 요약에_실패해도_문서는_READY로_저장된다(@TempDir Path dir) throws Exception {
        when(failingSummary.summarize(anyString())).thenThrow(new IllegalStateException("API 오류"));
        Path file = dir.resolve("rule.txt");
        Files.writeString(file, "학사 운영 규정 본문", StandardCharsets.UTF_8);
        Document document = new Document("규정", "rule.txt", FileType.TXT, null);

        pipeline.process(document, file);

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.READY);
        assertThat(document.getSummary()).isNull();
        assertThat(document.getChunks()).hasSize(1);
    }

    @Test
    void 텍스트가_없는_TXT는_형식에_맞는_문구로_거부한다(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("blank.txt");
        Files.writeString(file, "   ", StandardCharsets.UTF_8);
        Document document = new Document("빈 문서", "blank.txt", FileType.TXT, null);

        assertThatThrownBy(() -> pipeline.process(document, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("문서에서 텍스트를 찾을 수 없습니다.");
    }
}

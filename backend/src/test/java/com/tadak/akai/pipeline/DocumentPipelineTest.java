package com.tadak.akai.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.mockito.ArgumentCaptor;

class DocumentPipelineTest {

    private final SummaryGenerator summaryGenerator = mock(SummaryGenerator.class);
    private final DocumentPipeline pipeline = new DocumentPipeline(
            new TextExtractorFactory(List.of(new TxtTextExtractor())), new ChunkSplitter(), summaryGenerator);

    @Test
    void 요약에_성공하면_요약과_키워드가_AI_생성으로_저장된다(@TempDir Path dir) throws Exception {
        when(summaryGenerator.summarize(anyString())).thenReturn("학사 운영 규정 요약");
        when(summaryGenerator.extractKeywords(anyString())).thenReturn(List.of("학사", "수강", "성적", "졸업", "휴학"));
        Path file = dir.resolve("rule.txt");
        Files.writeString(file, "학사 운영 규정 본문", StandardCharsets.UTF_8);
        Document document = new Document("규정", "rule.txt", FileType.TXT, null);

        pipeline.process(document, file);

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.READY);
        assertThat(document.getSummary().getContent()).isEqualTo("학사 운영 규정 요약");
        assertThat(document.getSummary().getKeywords()).isEqualTo("학사, 수강, 성적, 졸업, 휴학");
        assertThat(document.getSummary().isAiGenerated()).isTrue();
    }

    @Test
    void 긴_본문은_요약_요청에_30000자까지만_보낸다(@TempDir Path dir) throws Exception {
        when(summaryGenerator.summarize(anyString())).thenReturn("요약");
        when(summaryGenerator.extractKeywords(anyString())).thenReturn(List.of("키워드"));
        Path file = dir.resolve("long.txt");
        Files.writeString(file, "가".repeat(30001), StandardCharsets.UTF_8);
        Document document = new Document("긴 문서", "long.txt", FileType.TXT, null);

        pipeline.process(document, file);

        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(summaryGenerator).summarize(sent.capture());
        assertThat(sent.getValue()).hasSize(30000);
    }

    @Test
    void 요약에_실패해도_문서는_READY로_저장된다(@TempDir Path dir) throws Exception {
        when(summaryGenerator.summarize(anyString())).thenThrow(new IllegalStateException("API 오류"));
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

package com.tadak.akai.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.FileType;
import com.tadak.akai.extractor.PageText;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChunkSplitterTest {

    private final ChunkSplitter splitter = new ChunkSplitter();
    private final Document document = new Document("테스트", "test.txt", FileType.TXT, null);

    @Test
    void 짧은_문단들은_한_조각으로_모은다() {
        List<Chunk> chunks = splitter.split(document, List.of(new PageText(1, "첫 문단\n\n둘째 문단")));

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getContent()).contains("첫 문단").contains("둘째 문단");
    }

    @Test
    void 페이지_번호와_순서를_기록한다() {
        List<Chunk> chunks = splitter.split(document, List.of(
                new PageText(1, "1쪽 내용"), new PageText(2, "2쪽 내용")));

        assertThat(chunks).extracting(Chunk::getPageNo).containsExactly(1, 2);
        assertThat(chunks).extracting(Chunk::getSeq).containsExactly(1, 2);
    }

    @Test
    void 긴_텍스트는_기준_길이를_넘지_않게_자른다() {
        String longText = "가".repeat(ChunkSplitter.TARGET_LENGTH * 3 + 10);

        List<Chunk> chunks = splitter.split(document, List.of(new PageText(1, longText)));

        assertThat(chunks).hasSize(4);
        assertThat(chunks).allSatisfy(chunk ->
                assertThat(chunk.getContent().length()).isLessThanOrEqualTo(ChunkSplitter.TARGET_LENGTH));
    }

    @Test
    void 빈_페이지는_조각을_만들지_않는다() {
        List<Chunk> chunks = splitter.split(document, List.of(new PageText(1, "  \n\n  ")));

        assertThat(chunks).isEmpty();
    }
}

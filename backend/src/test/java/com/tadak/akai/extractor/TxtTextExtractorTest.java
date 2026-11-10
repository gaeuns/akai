package com.tadak.akai.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TxtTextExtractorTest {

    private final TxtTextExtractor extractor = new TxtTextExtractor();

    @Test
    void UTF8_파일을_1페이지로_읽는다(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("a.txt");
        Files.writeString(file, "학사 운영 규정", StandardCharsets.UTF_8);

        List<PageText> pages = extractor.extract(file);

        assertThat(pages).containsExactly(new PageText(1, "학사 운영 규정"));
    }

    @Test
    void MS949_파일도_읽는다(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("b.txt");
        Files.writeString(file, "정보보안 매뉴얼", Charset.forName("MS949"));

        List<PageText> pages = extractor.extract(file);

        assertThat(pages.get(0).text()).isEqualTo("정보보안 매뉴얼");
    }
}

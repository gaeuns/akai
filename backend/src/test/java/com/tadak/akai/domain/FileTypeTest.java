package com.tadak.akai.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FileTypeTest {

    @Test
    void 확장자로_형식을_정한다() {
        assertThat(FileType.fromFileName("규정.PDF")).isEqualTo(FileType.PDF);
        assertThat(FileType.fromFileName("매뉴얼.txt")).isEqualTo(FileType.TXT);
    }

    @Test
    void 지원하지_않는_형식은_거부한다() {
        assertThatThrownBy(() -> FileType.fromFileName("보고서.docx"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

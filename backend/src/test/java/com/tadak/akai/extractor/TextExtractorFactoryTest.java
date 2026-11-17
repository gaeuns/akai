package com.tadak.akai.extractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tadak.akai.domain.FileType;
import java.util.List;
import org.junit.jupiter.api.Test;

class TextExtractorFactoryTest {

    @Test
    void 파일_형식에_맞는_추출기를_돌려준다() {
        TextExtractorFactory factory = new TextExtractorFactory(List.of(new PdfTextExtractor(), new TxtTextExtractor()));

        assertThat(factory.create(FileType.PDF)).isInstanceOf(PdfTextExtractor.class);
        assertThat(factory.create(FileType.TXT)).isInstanceOf(TxtTextExtractor.class);
    }

    @Test
    void 등록되지_않은_형식은_거부한다() {
        TextExtractorFactory factory = new TextExtractorFactory(List.of(new TxtTextExtractor()));

        assertThatThrownBy(() -> factory.create(FileType.PDF))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 파일 형식");
    }
}

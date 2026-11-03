package com.tadak.akai.extractor;

import com.tadak.akai.domain.FileType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Factory Method: 파일 형식에 맞는 TextExtractor를 돌려준다. 형식 추가 = 구현 클래스 추가. */
@Component
public class TextExtractorFactory {

    private final Map<FileType, TextExtractor> extractors = new EnumMap<>(FileType.class);

    public TextExtractorFactory(List<TextExtractor> extractors) {
        for (TextExtractor extractor : extractors) {
            this.extractors.put(extractor.supportedType(), extractor);
        }
    }

    public TextExtractor create(FileType fileType) {
        TextExtractor extractor = extractors.get(fileType);
        if (extractor == null) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + fileType);
        }
        return extractor;
    }
}

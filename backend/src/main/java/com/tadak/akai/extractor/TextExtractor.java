package com.tadak.akai.extractor;

import com.tadak.akai.domain.FileType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** 파일에서 페이지별 텍스트를 추출한다. 구현은 TextExtractorFactory가 형식에 맞게 고른다. */
public interface TextExtractor {

    FileType supportedType();

    List<PageText> extract(Path file) throws IOException;
}

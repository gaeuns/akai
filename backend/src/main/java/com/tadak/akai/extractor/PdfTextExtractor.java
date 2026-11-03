package com.tadak.akai.extractor;

import com.tadak.akai.domain.FileType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

/** Adapter: PDFBox를 TextExtractor 인터페이스로 감싼다. */
@Component
public class PdfTextExtractor implements TextExtractor {

    @Override
    public FileType supportedType() {
        return FileType.PDF;
    }

    @Override
    public List<PageText> extract(Path file) throws IOException {
        // TODO(F-01): PDFBox로 페이지별 텍스트 추출, 텍스트가 없는 스캔 PDF는 예외로 안내
        throw new UnsupportedOperationException("F-01 구현 예정");
    }
}

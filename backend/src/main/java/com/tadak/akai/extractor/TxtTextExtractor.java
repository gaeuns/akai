package com.tadak.akai.extractor;

import com.tadak.akai.domain.FileType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TxtTextExtractor implements TextExtractor {

    @Override
    public FileType supportedType() {
        return FileType.TXT;
    }

    @Override
    public List<PageText> extract(Path file) throws IOException {
        // TODO(F-01): 파일 전체를 읽어 1페이지로 반환
        throw new UnsupportedOperationException("F-01 구현 예정");
    }
}

package com.tadak.akai.extractor;

import com.tadak.akai.domain.FileType;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

/** TXT 파일 전체를 1페이지로 본다. UTF-8로 읽지 못하면 윈도우 한글 인코딩(MS949)으로 다시 읽는다. */
@Component
public class TxtTextExtractor implements TextExtractor {

    private static final Charset KOREAN_WINDOWS = Charset.forName("MS949");

    @Override
    public FileType supportedType() {
        return FileType.TXT;
    }

    @Override
    public List<PageText> extract(Path file) throws IOException {
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            text = Files.readString(file, KOREAN_WINDOWS);
        }
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        return List.of(new PageText(1, text));
    }
}

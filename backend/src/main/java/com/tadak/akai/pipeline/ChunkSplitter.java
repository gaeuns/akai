package com.tadak.akai.pipeline;

import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Document;
import com.tadak.akai.extractor.PageText;
import java.util.List;
import org.springframework.stereotype.Component;

/** 페이지별 텍스트를 문단 단위 조각으로 나눈다. */
@Component
public class ChunkSplitter {

    public List<Chunk> split(Document document, List<PageText> pages) {
        // TODO(F-01): 페이지·문단 단위로 분할, 페이지 번호와 순서(seq) 기록, 조각은 Chunk.MAX_CONTENT_LENGTH 이하로 자름
        throw new UnsupportedOperationException("F-01 구현 예정");
    }
}

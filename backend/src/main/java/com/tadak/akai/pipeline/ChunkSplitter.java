package com.tadak.akai.pipeline;

import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Document;
import com.tadak.akai.extractor.PageText;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 페이지별 텍스트를 조각으로 나눈다. 페이지 경계는 넘지 않고, 문단(빈 줄) 단위로 모아
 * 조각 하나가 TARGET_LENGTH 자를 넘지 않게 한다. 긴 문단은 줄 단위로, 긴 줄은 글자 수로 자른다.
 */
@Component
public class ChunkSplitter {

    static final int TARGET_LENGTH = 1000;

    public List<Chunk> split(Document document, List<PageText> pages) {
        List<Chunk> chunks = new ArrayList<>();
        int seq = 0;
        for (PageText page : pages) {
            for (String content : splitPage(page.text())) {
                seq++;
                chunks.add(new Chunk(document, page.pageNo(), seq, content));
            }
        }
        return chunks;
    }

    List<String> splitPage(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return pack(toUnits(text));
    }

    /** 문단을 기본 단위로 하되, TARGET_LENGTH보다 긴 문단은 줄로, 긴 줄은 글자 수로 쪼갠다. */
    private List<String> toUnits(String text) {
        List<String> units = new ArrayList<>();
        for (String paragraph : text.split("\\R\\s*\\R")) {
            String trimmed = paragraph.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.length() <= TARGET_LENGTH) {
                units.add(trimmed);
                continue;
            }
            for (String line : trimmed.split("\\R")) {
                addCut(units, line.strip());
            }
        }
        return units;
    }

    private void addCut(List<String> units, String line) {
        for (int start = 0; start < line.length(); start += TARGET_LENGTH) {
            String piece = line.substring(start, Math.min(line.length(), start + TARGET_LENGTH)).strip();
            if (!piece.isEmpty()) {
                units.add(piece);
            }
        }
    }

    /** 단위들을 순서대로 이어 붙이되 TARGET_LENGTH를 넘기 전에 새 조각을 시작한다. */
    private List<String> pack(List<String> units) {
        List<String> pieces = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String unit : units) {
            if (current.length() > 0 && current.length() + 1 + unit.length() > TARGET_LENGTH) {
                pieces.add(current.toString());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(unit);
        }
        if (current.length() > 0) {
            pieces.add(current.toString());
        }
        return pieces;
    }
}

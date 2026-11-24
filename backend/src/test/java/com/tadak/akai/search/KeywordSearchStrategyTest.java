package com.tadak.akai.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tadak.akai.domain.Chunk;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.FileType;
import com.tadak.akai.repository.ChunkRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

/** F-03 키워드 검색 기준: 키워드 절반 이상, 키워드가 2개 이상이면 최소 2개 일치 (PR #21) */
class KeywordSearchStrategyTest {

    private final ChunkRepository chunkRepository = mock(ChunkRepository.class);
    private final KeywordSearchStrategy strategy = new KeywordSearchStrategy(chunkRepository);
    private final Document document = new Document("매뉴얼", "manual.txt", FileType.TXT, null);

    @Test
    void 필요한_일치_개수는_절반_이상이고_키워드가_2개_이상이면_최소_2개다() {
        assertThat(KeywordSearchStrategy.minMatches(1)).isEqualTo(1);
        assertThat(KeywordSearchStrategy.minMatches(2)).isEqualTo(2);
        assertThat(KeywordSearchStrategy.minMatches(3)).isEqualTo(2);
        assertThat(KeywordSearchStrategy.minMatches(4)).isEqualTo(2);
        assertThat(KeywordSearchStrategy.minMatches(5)).isEqualTo(3);
    }

    @Test
    void 키워드_2개_중_1개만_든_조각은_근거로_쓰지_않는다() {
        Chunk installGuide = chunk("프린터 드라이버 설치 순서를 안내한다.");
        when(chunkRepository.findAll()).thenReturn(List.of(installGuide));

        assertThat(strategy.search("자바 설치 방법")).isEmpty();
    }

    @Test
    void 키워드_2개가_모두_든_조각은_근거로_쓴다() {
        Chunk javaGuide = chunk("자바 개발 도구 설치 순서를 안내한다.");
        when(chunkRepository.findAll()).thenReturn(List.of(javaGuide));

        assertThat(strategy.search("자바 설치 방법")).containsExactly(javaGuide);
    }

    @Test
    void 키워드가_1개인_질문은_1개만_맞아도_근거로_쓴다() {
        Chunk vpnGuide = chunk("VPN 접속 방법을 안내한다.");
        when(chunkRepository.findAll()).thenReturn(List.of(vpnGuide));

        assertThat(strategy.search("VPN?")).containsExactly(vpnGuide);
    }

    private Chunk chunk(String content) {
        return new Chunk(document, 1, content.length(), content);
    }
}

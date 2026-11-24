package com.tadak.akai.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.tadak.akai.search.EmbeddingSearchStrategy;
import com.tadak.akai.search.KeywordSearchStrategy;
import org.junit.jupiter.api.Test;

/** F-06 · Strategy: app.search.strategy 설정으로 검색 방식을 고른다 */
class AppConfigTest {

    private final AppConfig config = new AppConfig();
    private final KeywordSearchStrategy keyword = mock(KeywordSearchStrategy.class);
    private final EmbeddingSearchStrategy embedding = mock(EmbeddingSearchStrategy.class);

    @Test
    void keyword로_설정하면_키워드_검색을_쓴다() {
        assertThat(config.searchStrategy("keyword", keyword, embedding)).isSameAs(keyword);
    }

    @Test
    void 대소문자와_앞뒤_공백은_무시하고_embedding을_고른다() {
        assertThat(config.searchStrategy(" EMBEDDING ", keyword, embedding)).isSameAs(embedding);
    }

    @Test
    void 모르는_검색_방식이면_서버가_뜨지_않게_막는다() {
        assertThatThrownBy(() -> config.searchStrategy("vector", keyword, embedding))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("keyword 또는 embedding");
    }
}

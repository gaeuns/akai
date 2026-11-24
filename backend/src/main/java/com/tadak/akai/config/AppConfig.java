package com.tadak.akai.config;

import com.tadak.akai.search.EmbeddingSearchStrategy;
import com.tadak.akai.search.KeywordSearchStrategy;
import com.tadak.akai.search.SearchStrategy;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 공통 설정. F-06 · Strategy: app.search.strategy 값으로 SearchStrategy 구현을 고른다.
 * QaService는 SearchStrategy만 알기 때문에 검색 방식을 바꿔도 QaService는 고치지 않는다(OCP·DIP).
 */
@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    @Primary
    public SearchStrategy searchStrategy(
            @Value("${app.search.strategy:keyword}") String strategy,
            KeywordSearchStrategy keywordSearchStrategy,
            EmbeddingSearchStrategy embeddingSearchStrategy) {
        String name = strategy.strip().toLowerCase(Locale.ROOT);
        log.info("검색 방식: {}", name);
        return switch (name) {
            case "keyword" -> keywordSearchStrategy;
            case "embedding" -> embeddingSearchStrategy;
            default -> throw new IllegalStateException(
                    "app.search.strategy는 keyword 또는 embedding이어야 합니다: " + strategy);
        };
    }
}

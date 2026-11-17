package com.tadak.akai.search;

import com.tadak.akai.domain.Chunk;
import com.tadak.akai.repository.ChunkRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * MVP 검색: 질문에서 키워드를 뽑아 조각을 찾고, 키워드가 많이 들어 있는 조각부터 돌려준다.
 * 키워드의 절반 이상이 들어 있는 조각만 관련 조각으로 본다. 하나도 없으면 빈 목록을 돌려주고,
 * QaService는 AI를 부르지 않고 "문서에서 찾을 수 없음"으로 답한다.
 *
 * <p>PDF에서 뽑은 글은 줄바꿈이 단어 가운데 들어가기도 해서("등\n급") 공백·줄바꿈을 지우고 비교한다.
 * 그래서 DB LIKE 대신 조각을 모두 읽어 비교한다(시연 문서 수십 개 규모). 규모가 커지면
 * EmbeddingSearchStrategy(F-06)로 바꾼다.
 */
@Component
public class KeywordSearchStrategy implements SearchStrategy {

    /** AI에 보낼 조각 수 (조각 하나가 최대 약 1000자) */
    static final int MAX_RESULTS = 3;
    static final double MIN_MATCH_RATIO = 0.5;
    private static final int MIN_KEYWORD_LENGTH = 2;

    /** 긴 것부터 확인한다. 떼고 남은 말이 2글자보다 짧으면 떼지 않는다. */
    private static final List<String> SUFFIXES = List.of(
            "으로부터", "에서부터", "하려면", "되려면", "있나요", "없나요", "되나요", "하나요", "한가요", "인가요",
            "합니까", "됩니까", "입니까", "습니까", "이라도", "에게서", "으로는", "에서는", "에서도", "까지는",
            "부터는", "이란", "이라", "에서", "에게", "한테", "까지", "부터", "으로", "처럼", "보다", "이나",
            "하고", "하면", "하는", "되는", "해야", "인지", "한지", "나요", "가요", "은요", "는요",
            "은", "는", "이", "가", "을", "를", "에", "의", "로", "와", "과", "도", "만", "요", "한", "할", "된");

    /** 질문에서 붙여 쓰는 의문 접두어 ("몇퍼센트", "몇학기") */
    private static final List<String> PREFIXES = List.of("몇");

    /** 질문에 흔히 들어가지만 문서 내용을 가리키지 않는 말 */
    private static final Set<String> STOP_WORDS = Set.of(
            "무엇", "무엇인", "뭐야", "뭔가", "어떻게", "어떤", "언제", "어디", "누구", "얼마", "얼마나", "왜",
            "알려", "알려줘", "알려주세요", "설명", "설명해", "설명해줘", "대해", "대한", "관련", "경우",
            "가능", "가능한", "방법", "주세요", "해주세요", "그리고", "또는", "있는", "없는", "있어", "있나",
            "있을까", "하나", "할까", "되나", "받을", "받는", "받나", "받아야", "받으려면", "궁금", "궁금해",
            "the", "what", "how");

    /** 질문과 문서가 다르게 쓰는 말. 키워드는 목록 중 하나라도 들어 있으면 맞은 것으로 본다. */
    private static final Map<String, List<String>> SYNONYMS = Map.of(
            "퍼센트", List.of("퍼센트", "%", "비율"),
            "프로", List.of("프로", "%", "퍼센트"),
            "학점", List.of("학점", "등급"));

    private final ChunkRepository chunkRepository;

    public KeywordSearchStrategy(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    @Override
    public List<Chunk> search(String question) {
        List<String> keywords = extractKeywords(question);
        if (keywords.isEmpty()) {
            return List.of();
        }
        int minMatches = (int) Math.ceil(keywords.size() * MIN_MATCH_RATIO);
        Map<Chunk, Integer> matchCounts = new LinkedHashMap<>();
        for (Chunk chunk : chunkRepository.findAll()) {
            int count = countMatches(normalize(chunk.getContent()), keywords);
            if (count >= minMatches) {
                matchCounts.put(chunk, count);
            }
        }
        return matchCounts.entrySet().stream()
                .sorted(Map.Entry.<Chunk, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(MAX_RESULTS)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * 질문을 단어로 나누고 의문 접두어·조사·어미를 뗀 뒤, 불용어가 아닌 말만 남긴다.
     * 2글자 이상만 남기되, 학점 등급 같은 영문 한 글자("A")는 남긴다.
     */
    static List<String> extractKeywords(String question) {
        if (question == null) {
            return List.of();
        }
        Set<String> keywords = new LinkedHashSet<>();
        for (String word : question.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (word.isEmpty() || STOP_WORDS.contains(word)) {
                continue;
            }
            String stem = removeSuffix(removePrefix(word));
            boolean longEnough = stem.length() >= MIN_KEYWORD_LENGTH || stem.matches("[a-z]");
            if (longEnough && !STOP_WORDS.contains(stem)) {
                keywords.add(stem);
            }
        }
        return List.copyOf(keywords);
    }

    private static int countMatches(String content, List<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            List<String> alternatives = SYNONYMS.getOrDefault(keyword, List.of(keyword));
            if (alternatives.stream().anyMatch(content::contains)) {
                count++;
            }
        }
        return count;
    }

    /** 공백·줄바꿈을 지우고 소문자로 바꾼다 */
    private static String normalize(String content) {
        return content.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private static String removePrefix(String word) {
        for (String prefix : PREFIXES) {
            if (word.startsWith(prefix) && word.length() - prefix.length() >= MIN_KEYWORD_LENGTH) {
                return word.substring(prefix.length());
            }
        }
        return word;
    }

    private static String removeSuffix(String word) {
        for (String suffix : SUFFIXES) {
            if (word.endsWith(suffix) && word.length() - suffix.length() >= MIN_KEYWORD_LENGTH) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return word;
    }
}

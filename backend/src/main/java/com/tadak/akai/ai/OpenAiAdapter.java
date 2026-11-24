package com.tadak.akai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.tadak.akai.domain.Chunk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adapter: OpenAI API를 SummaryGenerator·AnswerGenerator·EmbeddingGenerator로 감싼다. API 키는 여기서만 읽는다.
 */
@Component
public class OpenAiAdapter implements SummaryGenerator, AnswerGenerator, EmbeddingGenerator {

    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";
    private static final int KEYWORD_COUNT = 5;
    /** 임베딩 요청 한 번에 보내는 글 수 */
    private static final int EMBEDDING_BATCH_SIZE = 100;

    private final String apiKey;
    private final String model;
    private final String embeddingModel;
    private final RestClient restClient;

    public OpenAiAdapter(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:}") String model,
            @Value("${openai.embedding-model:}") String embeddingModel) {
        this.apiKey = apiKey;
        this.model = (model == null || model.isBlank()) ? DEFAULT_MODEL : model;
        this.embeddingModel = (embeddingModel == null || embeddingModel.isBlank())
                ? DEFAULT_EMBEDDING_MODEL : embeddingModel;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(30000);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String summarize(String text) {
        String instruction = "너는 기관 문서를 요약하는 도우미다. 주어진 문서 내용만 사용해 한국어 5문장 이내로 요약하라. "
                + "문서에 없는 내용은 추가하지 마라.";
        return chat(instruction, text);
    }

    @Override
    public List<String> extractKeywords(String text) {
        String instruction = "주어진 문서의 핵심 키워드 " + KEYWORD_COUNT + "개를 한국어로 뽑아라. "
                + "설명 없이 쉼표로만 구분해 한 줄로 답하라.";
        String answer = chat(instruction, text);
        return Arrays.stream(answer.split("[,\\n]"))
                .map(OpenAiAdapter::removeListMarker)
                .filter(keyword -> !keyword.isEmpty())
                .limit(KEYWORD_COUNT)
                .toList();
    }

    /**
     * AI가 목록으로 답했을 때 붙는 기호("1. ", "2) ", "- ", "#")만 지운다.
     * "2단계 인증"처럼 숫자로 시작하는 키워드의 숫자는 남긴다 (결함 D-08).
     */
    private static String removeListMarker(String keyword) {
        return keyword.replaceAll("^\\s*(?:[#*•\\-]+|\\d+[.)])\\s*", "").strip();
    }

    /** 프롬프트에는 질문과 전달받은 조각만 넣는다 (AI 기능 규칙 1) */
    @Override
    public String generateAnswer(String question, List<Chunk> chunks) {
        String instruction = "너는 기관 문서 Q&A 도우미다. [근거]에 적힌 내용만 사용해 [질문]에 한국어로 답하라. "
                + "근거에 없는 내용은 추측하거나 덧붙이지 마라. 5문장 이내로 답하라. "
                + "근거로 답할 수 없으면 다른 말 없이 \"" + NOT_FOUND_MESSAGE + "\"이라고만 답하라.";
        return chat(instruction, buildAnswerPrompt(question, chunks));
    }

    private String buildAnswerPrompt(String question, List<Chunk> chunks) {
        StringBuilder prompt = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            prompt.append("[근거 ").append(i + 1).append("] ")
                    .append(chunk.getDocument().getTitle()).append(", ").append(chunk.getPageNo()).append("쪽\n")
                    .append(chunk.getContent()).append("\n\n");
        }
        return prompt.append("[질문]\n").append(question).toString();
    }

    /** F-06: OpenAI Embeddings API. 글이 많으면 나눠서 보낸다. */
    @Override
    public List<float[]> embedAll(List<String> texts) {
        List<float[]> vectors = new ArrayList<>();
        for (int from = 0; from < texts.size(); from += EMBEDDING_BATCH_SIZE) {
            List<String> batch = texts.subList(from, Math.min(from + EMBEDDING_BATCH_SIZE, texts.size()));
            JsonNode response = post("/embeddings", Map.of("model", embeddingModel, "input", batch));
            JsonNode data = response.path("data");
            if (data.size() != batch.size()) {
                throw new AiServiceException("임베딩 응답 개수가 맞지 않습니다.");
            }
            for (JsonNode item : data) {
                JsonNode values = item.path("embedding");
                float[] vector = new float[values.size()];
                for (int i = 0; i < vector.length; i++) {
                    vector[i] = (float) values.get(i).asDouble();
                }
                vectors.add(vector);
            }
        }
        return vectors;
    }

    private String chat(String instruction, String content) {
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", instruction),
                        Map.of("role", "user", "content", content)));
        JsonNode response = post("/chat/completions", body);
        return response.path("choices").path(0).path("message").path("content").asText("").strip();
    }

    private JsonNode post(String uri, Map<String, Object> body) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException("OPENAI_API_KEY가 설정되지 않았습니다. backend/.env를 확인하세요.");
        }
        JsonNode response;
        try {
            response = restClient.post()
                    .uri(uri)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new AiServiceException("OpenAI 호출 실패: " + e.getMessage(), e);
        }
        if (response == null) {
            throw new AiServiceException("OpenAI 응답이 비어 있습니다.");
        }
        return response;
    }
}

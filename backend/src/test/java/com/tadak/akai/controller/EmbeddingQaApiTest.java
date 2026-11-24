package com.tadak.akai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tadak.akai.ai.AiServiceException;
import com.tadak.akai.ai.OpenAiAdapter;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.domain.Chunk;
import com.tadak.akai.dto.QuestionRequest;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.QuestionRepository;
import com.tadak.akai.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * F-06 질문하기를 임베딩 검색으로 했을 때(app.search.strategy=embedding).
 * 임베딩은 Mockito로 대신한다: "대출"이 들어간 글은 (1,0,ε), "비밀번호"는 (0,1,ε), 나머지는 (0,0,ε) 방향.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:embedding-qa-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads-embedding-qa",
    "app.search.strategy=embedding",
    "app.search.embedding.min-similarity=0.35"
})
class EmbeddingQaApiTest {

    private static final String LIBRARY_RULE = "중앙도서관 대출 규정. 학부생은 도서를 5권까지 14일 동안 대출할 수 있다.";
    private static final String SECURITY_RULE = "정보보안 매뉴얼. 비밀번호는 90일마다 변경한다.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private OpenAiAdapter openAiAdapter;

    @BeforeEach
    void setUp() throws Exception {
        questionRepository.deleteAll();
        documentRepository.deleteAll();
        when(openAiAdapter.summarize(anyString())).thenReturn("요약");
        when(openAiAdapter.extractKeywords(anyString())).thenReturn(List.of("키워드"));
        when(openAiAdapter.embedAll(anyList())).thenAnswer(invocation -> {
            List<String> texts = invocation.getArgument(0);
            return texts.stream().map(EmbeddingQaApiTest::fakeEmbedding).toList();
        });
        upload("library.txt", LIBRARY_RULE);
        upload("security.txt", SECURITY_RULE);
    }

    @Test
    void 표현이_달라도_의미가_가까운_조각만_AI에_보낸다() throws Exception {
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenReturn("14일 동안 빌릴 수 있습니다.");

        ask("책을 대출하면 언제까지 반납하나요?")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.evidences", hasSize(1)))
                .andExpect(jsonPath("$.evidences[0].documentTitle").value("library"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Chunk>> sent = ArgumentCaptor.forClass(List.class);
        verify(openAiAdapter).generateAnswer(any(), sent.capture());
        assertThat(sent.getValue()).extracting(Chunk::getContent).containsExactly(LIBRARY_RULE);
    }

    @Test
    void 유사도가_기준보다_낮으면_AI를_부르지_않고_찾을_수_없음으로_답한다() throws Exception {
        ask("기숙사 입사 신청은 언제 하나요?")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.answer").value("문서에서 찾을 수 없음"))
                .andExpect(jsonPath("$.found").value(false));

        verify(openAiAdapter, never()).generateAnswer(anyString(), anyList());
    }

    @Test
    void 임베딩_API가_실패하면_키워드_검색으로_대신해_답한다() throws Exception {
        when(openAiAdapter.embedAll(anyList())).thenThrow(new AiServiceException("시간 초과"));
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenReturn("비밀번호는 90일마다 바꿉니다.");

        ask("비밀번호 변경 주기는?")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.evidences[0].documentTitle").value("security"));
    }

    private static float[] fakeEmbedding(String text) {
        return new float[] {text.contains("대출") ? 1f : 0f, text.contains("비밀번호") ? 1f : 0f, 0.01f};
    }

    private ResultActions ask(String question) throws Exception {
        return mockMvc.perform(post("/api/questions")
                .with(user(login("member01")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new QuestionRequest(question))));
    }

    private void upload(String fileName, String content) throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", fileName, "text/plain", content.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/documents").file(file).with(user(login("admin01"))))
                .andExpect(status().isCreated());
    }

    private LoginUser login(String loginId) {
        return LoginUser.from(userRepository.findByLoginId(loginId).orElseThrow());
    }
}

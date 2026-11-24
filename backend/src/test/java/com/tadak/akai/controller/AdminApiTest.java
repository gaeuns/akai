package com.tadak.akai.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tadak.akai.ai.AiServiceException;
import com.tadak.akai.ai.OpenAiAdapter;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.dto.QuestionRequest;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.QuestionRepository;
import com.tadak.akai.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** F-09 품질 지표, F-10 검색 방식 비교 (/api/admin/**, 관리자만). OpenAI는 Mockito로 대신한다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:admin-api-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads-admin-api",
    "app.search.strategy=keyword"
})
class AdminApiTest {

    private static final String LIBRARY_RULE = "중앙도서관 대출 규정. 학부생은 도서를 5권까지 14일 동안 대출할 수 있다.";

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
        when(openAiAdapter.generateAnswer(anyString(), anyList())).thenReturn("14일 동안 빌릴 수 있습니다.");
        MockMultipartFile file = new MockMultipartFile(
                "file", "library.txt", "text/plain", LIBRARY_RULE.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/documents").file(file).with(user(login("admin01"))))
                .andExpect(status().isCreated());
    }

    @Test
    void 질문이_없으면_비율과_평균은_비어_있다() throws Exception {
        statistics("admin01")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchStrategy").value("keyword"))
                .andExpect(jsonPath("$.documentCount").value(1))
                .andExpect(jsonPath("$.questionCount").value(0))
                .andExpect(jsonPath("$.notFoundRate", nullValue()))
                .andExpect(jsonPath("$.averageResponseSeconds", nullValue()))
                .andExpect(jsonPath("$.within5sRate", nullValue()));
    }

    @Test
    void 질문하면_찾을_수_없음_비율_응답_시간_근거_문서가_집계된다() throws Exception {
        ask("학부생 도서 대출 기간은?");
        ask("기숙사 입사 신청은 언제 하나요?");

        statistics("admin01")
                .andExpect(jsonPath("$.questionCount").value(2))
                .andExpect(jsonPath("$.notFoundCount").value(1))
                .andExpect(jsonPath("$.notFoundRate").value(50.0))
                .andExpect(jsonPath("$.averageResponseSeconds", notNullValue()))
                .andExpect(jsonPath("$.within5sRate").value(100.0))
                .andExpect(jsonPath("$.topDocuments[0].title").value("library"))
                .andExpect(jsonPath("$.topDocuments[0].evidenceCount").value(1))
                .andExpect(jsonPath("$.recentNotFound[0].question").value("기숙사 입사 신청은 언제 하나요?"));
    }

    @Test
    void 검색_방식_비교는_키워드와_임베딩_결과를_나란히_돌려준다() throws Exception {
        when(openAiAdapter.embedAll(anyList())).thenAnswer(invocation -> {
            List<String> texts = invocation.getArgument(0);
            return texts.stream().map(text -> new float[] {text.contains("대출") ? 1f : 0f, 0.1f}).toList();
        });

        compareSearch("admin01", "학부생 도서 대출 기간은?")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyword.criterion").value("키워드 절반 이상 포함"))
                .andExpect(jsonPath("$.keyword.keywords", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.keyword.results[0].documentTitle").value("library"))
                .andExpect(jsonPath("$.keyword.results[0].relevant").value(true))
                .andExpect(jsonPath("$.embedding.error", nullValue()))
                .andExpect(jsonPath("$.embedding.results[0].relevant").value(true))
                .andExpect(jsonPath("$.embedding.results[0].score").value(greaterThanOrEqualTo(0.35)));
    }

    @Test
    void 임베딩이_실패해도_키워드_결과는_보이고_실패_안내가_붙는다() throws Exception {
        when(openAiAdapter.embedAll(anyList())).thenThrow(new AiServiceException("시간 초과"));

        compareSearch("admin01", "학부생 도서 대출 기간은?")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyword.results", hasSize(1)))
                .andExpect(jsonPath("$.embedding.error").value(containsString("임베딩 검색 실패")))
                .andExpect(jsonPath("$.embedding.results", hasSize(0)));
    }

    @Test
    void 검색_비교에_빈_질문을_보내면_400() throws Exception {
        compareSearch("admin01", "  ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("질문을 입력하세요."));
    }

    @Test
    void 이용자가_관리자_API를_호출하면_403() throws Exception {
        statistics("member01").andExpect(status().isForbidden());
        compareSearch("member01", "도서 대출").andExpect(status().isForbidden());
    }

    @Test
    void 로그인하지_않으면_관리자_API도_401() throws Exception {
        mockMvc.perform(get("/api/admin/statistics")).andExpect(status().isUnauthorized());
    }

    private ResultActions statistics(String loginId) throws Exception {
        return mockMvc.perform(get("/api/admin/statistics").with(user(login(loginId))));
    }

    private ResultActions compareSearch(String loginId, String question) throws Exception {
        return mockMvc.perform(post("/api/admin/search-comparison")
                .with(user(login(loginId)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new QuestionRequest(question))));
    }

    private void ask(String question) throws Exception {
        mockMvc.perform(post("/api/questions")
                        .with(user(login("member01")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QuestionRequest(question))))
                .andExpect(status().isCreated());
    }

    private LoginUser login(String loginId) {
        return LoginUser.from(userRepository.findByLoginId(loginId).orElseThrow());
    }
}

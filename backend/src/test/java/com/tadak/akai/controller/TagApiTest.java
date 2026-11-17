package com.tadak.akai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tadak.akai.config.LoginUser;
import com.tadak.akai.domain.Document;
import com.tadak.akai.domain.FileType;
import com.tadak.akai.domain.Summary;
import com.tadak.akai.repository.CategoryRepository;
import com.tadak.akai.repository.DocumentRepository;
import com.tadak.akai.repository.TagRepository;
import com.tadak.akai.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** F-04 · UC-03 · UC-05: 테스트 계획서 TT-01·TT-02, LT-03(분류·태그 API 권한). */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:tag-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads",
    "openai.api-key="
})
class TagApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clean() {
        documentRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // ---- 분류·태그 추가·수정 (TT-01) ----

    @Test
    void 관리자가_분류를_추가하면_201이고_목록에_보인다() throws Exception {
        asAdmin(post("/api/categories"), Map.of("name", "  규정·지침 "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("규정·지침"));

        mockMvc.perform(get("/api/categories").with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("규정·지침"));
    }

    @Test
    void 태그_목록은_이름순이다() throws Exception {
        createTag("학사");
        createTag("성적");

        mockMvc.perform(get("/api/tags").with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("성적"))
                .andExpect(jsonPath("$[1].name").value("학사"));
    }

    @Test
    void 태그_이름을_수정하면_바뀐_이름으로_저장된다() throws Exception {
        long tagId = createTag("수강");

        asAdmin(put("/api/tags/" + tagId), Map.of("name", "수강신청"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수강신청"));

        assertThat(tagRepository.findById(tagId).orElseThrow().getName()).isEqualTo("수강신청");
    }

    @Test
    void 이름을_자기_이름_그대로_수정해도_200() throws Exception {
        long categoryId = createCategory("강의자료");

        asAdmin(put("/api/categories/" + categoryId), Map.of("name", "강의자료"))
                .andExpect(status().isOk());
    }

    @Test
    void 빈_이름은_400() throws Exception {
        asAdmin(post("/api/tags"), Map.of("name", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("태그 이름을 입력하세요."));

        assertThat(tagRepository.count()).isZero();
    }

    @Test
    void 이름이_50자를_넘으면_400() throws Exception {
        asAdmin(post("/api/categories"), Map.of("name", "가".repeat(51)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("50자 이하")));
    }

    @Test
    void 이미_있는_이름으로_추가하면_400() throws Exception {
        createTag("학사");

        asAdmin(post("/api/tags"), Map.of("name", "학사"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 있는 태그입니다: 학사"));

        assertThat(tagRepository.count()).isEqualTo(1);
    }

    @Test
    void 다른_태그의_이름으로_수정하면_400() throws Exception {
        createTag("학사");
        long otherId = createTag("성적");

        asAdmin(put("/api/tags/" + otherId), Map.of("name", "학사"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 없는_분류를_수정하면_404() throws Exception {
        asAdmin(put("/api/categories/9999"), Map.of("name", "업무 매뉴얼"))
                .andExpect(status().isNotFound());
    }

    // ---- 문서에 분류·태그 붙이기와 검색 (TT-01, UC-05) ----

    @Test
    void 문서에_분류와_태그를_붙이면_상세에_보이고_태그로_검색된다() throws Exception {
        long categoryId = createCategory("규정·지침");
        long tagId = createTag("휴학");
        Document document = saveDocument("가온대학교 학사운영규정", "학사운영, 수강신청");

        asAdmin(put("/api/documents/" + document.getId() + "/classification"),
                Map.of("categoryId", categoryId, "tagIds", new long[] {tagId}))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.name").value("규정·지침"))
                .andExpect(jsonPath("$.tags[0].name").value("휴학"));

        mockMvc.perform(get("/api/documents").param("keyword", "휴학").with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("가온대학교 학사운영규정"))
                .andExpect(jsonPath("$[0].category.name").value("규정·지침"))
                .andExpect(jsonPath("$[0].tags[0].name").value("휴학"));
    }

    @Test
    void 제목과_키워드로도_검색된다() throws Exception {
        saveDocument("연구비 집행지침", "연구비, 정산");
        saveDocument("정보보안 매뉴얼", "보안, 비밀번호");

        mockMvc.perform(get("/api/documents").param("keyword", "연구비").with(user(member())))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("연구비 집행지침"));
        mockMvc.perform(get("/api/documents").param("keyword", "비밀번호").with(user(member())))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("정보보안 매뉴얼"));
    }

    @Test
    void 태그가_여러_개_맞아도_문서는_한_번만_나온다() throws Exception {
        long first = createTag("학사 일정");
        long second = createTag("학사 규정");
        Document document = saveDocument("학사운영규정", null);
        classify(document.getId(), null, first, second);

        mockMvc.perform(get("/api/documents").param("keyword", "학사").with(user(member())))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void 분류를_고르면_그_분류의_문서만_나온다() throws Exception {
        long rules = createCategory("규정·지침");
        long manuals = createCategory("업무 매뉴얼");
        Document rule = saveDocument("학사운영규정", "학사");
        Document manual = saveDocument("정보보안 매뉴얼", "보안");
        classify(rule.getId(), rules);
        classify(manual.getId(), manuals);

        mockMvc.perform(get("/api/documents").param("categoryId", String.valueOf(manuals)).with(user(member())))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("정보보안 매뉴얼"));
        mockMvc.perform(get("/api/documents")
                        .param("categoryId", String.valueOf(manuals))
                        .param("keyword", "학사")
                        .with(user(member())))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 분류를_비우고_태그를_빈_목록으로_보내면_모두_떨어진다() throws Exception {
        long categoryId = createCategory("규정·지침");
        long tagId = createTag("학사");
        Document document = saveDocument("학사운영규정", null);
        classify(document.getId(), categoryId, tagId);

        asAdmin(put("/api/documents/" + document.getId() + "/classification"), Map.of("tagIds", new long[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.tags.length()").value(0));
    }

    @Test
    void 없는_태그를_붙이면_404이고_기존_분류는_그대로다() throws Exception {
        long categoryId = createCategory("규정·지침");
        Document document = saveDocument("학사운영규정", null);
        classify(document.getId(), categoryId);

        asAdmin(put("/api/documents/" + document.getId() + "/classification"),
                Map.of("tagIds", new long[] {9999}))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("태그를 찾을 수 없습니다: 9999"));

        mockMvc.perform(get("/api/documents/" + document.getId()).with(user(member())))
                .andExpect(jsonPath("$.category.name").value("규정·지침"));
    }

    @Test
    void 없는_문서에_분류를_붙이면_404() throws Exception {
        asAdmin(put("/api/documents/9999/classification"), Map.of("tagIds", new long[0]))
                .andExpect(status().isNotFound());
    }

    // ---- 삭제 (TT-02: 집합 관계라 문서는 남는다) ----

    @Test
    void 분류를_삭제해도_문서는_남고_미분류가_된다() throws Exception {
        long categoryId = createCategory("규정·지침");
        Document document = saveDocument("학사운영규정", null);
        classify(document.getId(), categoryId);

        asAdmin(delete("/api/categories/" + categoryId), null)
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.count()).isZero();
        mockMvc.perform(get("/api/documents/" + document.getId()).with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    @Test
    void 태그를_삭제해도_문서는_남고_다른_태그는_유지된다() throws Exception {
        long removed = createTag("학사");
        long kept = createTag("휴학");
        Document document = saveDocument("학사운영규정", null);
        classify(document.getId(), null, removed, kept);

        asAdmin(delete("/api/tags/" + removed), null)
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/documents/" + document.getId()).with(user(member())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags.length()").value(1))
                .andExpect(jsonPath("$.tags[0].name").value("휴학"));
    }

    @Test
    void 문서를_삭제해도_분류와_태그는_남는다() throws Exception {
        long categoryId = createCategory("규정·지침");
        long tagId = createTag("학사");
        Document document = saveDocument("학사운영규정", null);
        classify(document.getId(), categoryId, tagId);

        asAdmin(delete("/api/documents/" + document.getId()), null)
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.count()).isEqualTo(1);
        assertThat(tagRepository.count()).isEqualTo(1);
    }

    // ---- 권한 (LT-03 · NF-03) ----

    @Test
    void 이용자가_분류_태그를_바꾸면_403() throws Exception {
        long tagId = createTag("학사");
        Document document = saveDocument("학사운영규정", null);

        asMember(post("/api/categories"), Map.of("name", "규정·지침")).andExpect(status().isForbidden());
        asMember(post("/api/tags"), Map.of("name", "성적")).andExpect(status().isForbidden());
        asMember(put("/api/tags/" + tagId), Map.of("name", "성적")).andExpect(status().isForbidden());
        asMember(delete("/api/tags/" + tagId), null).andExpect(status().isForbidden());
        asMember(put("/api/documents/" + document.getId() + "/classification"), Map.of("tagIds", new long[] {tagId}))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("관리자만 사용할 수 있는 기능입니다."));

        assertThat(categoryRepository.count()).isZero();
        assertThat(tagRepository.findById(tagId).orElseThrow().getName()).isEqualTo("학사");
    }

    @Test
    void 로그인하지_않으면_분류_목록도_401() throws Exception {
        mockMvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    }

    // ---- 도우미 ----

    private long createCategory(String name) throws Exception {
        return idOf(asAdmin(post("/api/categories"), Map.of("name", name)).andExpect(status().isCreated()));
    }

    private long createTag(String name) throws Exception {
        return idOf(asAdmin(post("/api/tags"), Map.of("name", name)).andExpect(status().isCreated()));
    }

    private void classify(Long documentId, Long categoryId, long... tagIds) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("categoryId", categoryId);
        body.put("tagIds", tagIds);
        asAdmin(put("/api/documents/" + documentId + "/classification"), body).andExpect(status().isOk());
    }

    private Document saveDocument(String title, String keywords) {
        Document document = new Document(title, title + ".txt", FileType.TXT,
                userRepository.findByLoginId("admin01").orElseThrow());
        if (keywords != null) {
            document.assignSummary(new Summary(document, "요약", keywords));
        }
        return documentRepository.save(document);
    }

    private ResultActions asAdmin(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return send(request, body, admin());
    }

    private ResultActions asMember(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return send(request, body, member());
    }

    private ResultActions send(MockHttpServletRequestBuilder request, Object body, LoginUser loginUser)
            throws Exception {
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(request.with(user(loginUser)));
    }

    private long idOf(ResultActions result) throws Exception {
        JsonNode node = objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private LoginUser admin() {
        return LoginUser.from(userRepository.findByLoginId("admin01").orElseThrow());
    }

    private LoginUser member() {
        return LoginUser.from(userRepository.findByLoginId("member01").orElseThrow());
    }
}

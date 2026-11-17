package com.tadak.akai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** F-08 · NF-03: 테스트 계획서 LT-01~LT-05. 초기 계정(admin01, member01, 비밀번호 akai1234)으로 실제 로그인한다. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:auth-test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.upload-dir=build/test-uploads",
    "openai.api-key="
})
class AuthApiTest {

    private static final String PASSWORD = "akai1234";

    @Autowired
    private MockMvc mockMvc;

    // LT-01
    @Test
    void 관리자_로그인은_200이고_역할은_ADMIN() throws Exception {
        login("admin01", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("admin01"))
                .andExpect(jsonPath("$.name").value("김관리"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void 이용자_로그인은_200이고_역할은_MEMBER() throws Exception {
        login("member01", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    // LT-02
    @Test
    void 틀린_비밀번호는_401과_실패_안내() throws Exception {
        login("admin01", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 없는_계정은_401과_같은_실패_안내() throws Exception {
        login("nobody", PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 빈_아이디는_400() throws Exception {
        login(" ", PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디와 비밀번호를 입력하세요."));
    }

    // LT-03
    @Test
    void 이용자가_업로드_API를_호출하면_403() throws Exception {
        MockHttpSession session = loginSession("member01");
        MockMultipartFile file = new MockMultipartFile(
                "file", "rule.txt", "text/plain", "내용".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/documents").file(file).session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("관리자만 사용할 수 있는 기능입니다."));
    }

    @Test
    void 이용자가_삭제_API를_호출하면_403() throws Exception {
        MockHttpSession session = loginSession("member01");

        mockMvc.perform(delete("/api/documents/1").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void 이용자가_태그_수정_API를_호출하면_403() throws Exception {
        MockHttpSession session = loginSession("member01");

        mockMvc.perform(post("/api/tags").session(session))
                .andExpect(status().isForbidden());
    }

    // LT-04
    @Test
    void 로그인_없이_API를_호출하면_401() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    void 로그인_없이_업로드하면_401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "rule.txt", "text/plain", "내용".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isUnauthorized());
    }

    // LT-05
    @Test
    void 관리자도_이용자_기능인_문서_목록을_조회한다() throws Exception {
        MockHttpSession session = loginSession("admin01");

        mockMvc.perform(get("/api/documents").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 이용자는_문서_목록을_조회한다() throws Exception {
        MockHttpSession session = loginSession("member01");

        mockMvc.perform(get("/api/documents").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void 로그인한_세션으로_내_정보를_조회한다() throws Exception {
        MockHttpSession session = loginSession("member01");

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("member01"));
    }

    @Test
    void 로그아웃하면_204이고_이후_요청은_401() throws Exception {
        MockHttpSession session = loginSession("member01");

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions login(String loginId, String password) throws Exception {
        String body = String.format("{\"loginId\":\"%s\",\"password\":\"%s\"}", loginId, password);
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private MockHttpSession loginSession(String loginId) throws Exception {
        return (MockHttpSession) login(loginId, PASSWORD)
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }
}

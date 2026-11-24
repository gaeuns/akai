package com.tadak.akai.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * F-08 · NF-03: 권한 검사는 화면이 아니라 여기서 한다.
 * 조회(GET)와 질문은 로그인한 사용자 모두, 문서·분류·태그를 바꾸는 요청과 /api/admin/** 는 관리자만 허용한다.
 */
@Configuration
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        // CSRF 토큰 대신 세션 쿠키를 SameSite=Strict로 보낸다(application.properties)
        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                // H2 콘솔 서블릿이 함께 등록되어 있어 경로는 antMatcher로 지정한다
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher("/api/auth/login")).permitAll()
                        .requestMatchers(antMatcher("/api/admin/**")).hasRole(LoginUser.ROLE_ADMIN)
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/**")).authenticated()
                        .requestMatchers(antMatcher("/api/questions/**")).authenticated()
                        .requestMatchers(
                                antMatcher("/api/documents/**"),
                                antMatcher("/api/categories/**"),
                                antMatcher("/api/tags/**")).hasRole(LoginUser.ROLE_ADMIN)
                        .requestMatchers(antMatcher("/api/**")).authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, e) ->
                                writeError(response, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."))
                        .accessDeniedHandler((request, response, e) ->
                                writeError(response, HttpStatus.FORBIDDEN, "관리자만 사용할 수 있는 기능입니다.")))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}

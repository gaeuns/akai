package com.tadak.akai.controller;

import com.tadak.akai.config.LoginUser;
import com.tadak.akai.dto.LoginRequest;
import com.tadak.akai.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** UC-01 · F-08: 로그인과 현재 사용자 조회. 로그아웃(POST /api/auth/logout)은 SecurityConfig가 처리한다. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    /** 성공하면 세션에 로그인 정보를 저장한다. 실패하면 ApiExceptionHandler가 401로 바꾼다. */
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        if (loginRequest.isBlank()) {
            throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        }
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.loginId().strip(), loginRequest.password()));

        renewSessionId(request);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return LoginResponse.from((LoginUser) authentication.getPrincipal());
    }

    @GetMapping("/me")
    public LoginResponse me(@AuthenticationPrincipal LoginUser loginUser) {
        return LoginResponse.from(loginUser);
    }

    /** 로그인 전 세션 ID를 그대로 쓰지 않게 바꾼다(세션 고정 공격 방지). */
    private void renewSessionId(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
    }
}

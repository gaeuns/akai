package com.tadak.akai.dto;

import com.tadak.akai.config.LoginUser;

/** 화면이 메뉴를 고를 때 쓰는 로그인 정보. role은 ADMIN 또는 MEMBER. */
public record LoginResponse(String loginId, String name, String role) {

    public static LoginResponse from(LoginUser loginUser) {
        return new LoginResponse(loginUser.getUsername(), loginUser.getName(), loginUser.getRole());
    }
}

package com.tadak.akai.config;

import com.tadak.akai.domain.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** 로그인한 사용자. 세션에 저장되고, 컨트롤러에서는 @AuthenticationPrincipal로 받는다. */
public class LoginUser implements UserDetails, CredentialsContainer {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";

    private final Long userId;
    private final String loginId;
    private final String name;
    private final String role;
    private String passwordHash;

    private LoginUser(Long userId, String loginId, String passwordHash, String name, String role) {
        this.userId = userId;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
    }

    public static LoginUser from(User user) {
        String role = user.canManageDocuments() ? ROLE_ADMIN : ROLE_MEMBER;
        return new LoginUser(user.getId(), user.getLoginId(), user.getPasswordHash(), user.getName(), role);
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** 인증이 끝나면 세션에 비밀번호 해시가 남지 않게 지운다. */
    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}

package com.tadak.akai.service;

import com.tadak.akai.config.LoginUser;
import com.tadak.akai.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** F-08: 로그인 아이디로 미리 등록된 계정을 찾는다. 비밀번호 비교는 Spring Security가 한다. */
@Service
@Transactional(readOnly = true)
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public LoginUser loadUserByUsername(String loginId) {
        return userRepository.findByLoginId(loginId)
                .map(LoginUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("없는 계정: " + loginId));
    }
}

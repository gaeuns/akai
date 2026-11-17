package com.tadak.akai.config;

import com.tadak.akai.domain.Admin;
import com.tadak.akai.domain.Member;
import com.tadak.akai.domain.User;
import com.tadak.akai.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 회원가입이 없으므로 계정은 미리 등록한다. users 테이블이 비어 있을 때만
 * db/sample-data.sql과 같은 계정(관리자 2, 이용자 4)을 만든다.
 */
@Component
@ConditionalOnProperty(name = "app.initial-accounts.enabled", havingValue = "true", matchIfMissing = true)
public class InitialAccountLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAccountLoader.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialPassword;

    public InitialAccountLoader(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.initial-accounts.password}") String initialPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialPassword = initialPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(initialPassword);
        List<User> accounts = List.of(
                new Admin("admin01", hash, "김관리"),
                new Admin("admin02", hash, "이문서"),
                new Member("member01", hash, "박학생"),
                new Member("member02", hash, "최연구"),
                new Member("member03", hash, "정교직"),
                new Member("member04", hash, "한민원"));
        userRepository.saveAll(accounts);
        log.info("초기 계정 {}개를 만들었습니다 (admin01·admin02 관리자, member01~04 이용자)", accounts.size());
    }
}

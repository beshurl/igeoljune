package com.skala.ikgeoljune;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// 폼 로그인용 기본 사용자는 쓰지 않는다. 인증은 JwtAuthenticationFilter 가 담당한다.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class IkgeoljuneApplication {

    public static void main(String[] args) {
        SpringApplication.run(IkgeoljuneApplication.class, args);
    }
}

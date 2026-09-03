package com.skala.ikgeoljune.security;

/** 인증된 사용자 정보. SecurityContext 의 principal 로 사용한다. */
public record AuthUser(Long userId, String email) {
}

package com.sinsaflower.server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = "com.sinsaflower.server.domain.*.repository"
)
public class JpaConfig {
    
    // JPA Auditing 설정
    // @CreatedDate, @LastModifiedDate 등 자동 적용
    
    // Repository 스캔 설정
    // domain 패키지의 모든 repository 자동 스캔
} 
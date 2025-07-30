package com.sinsaflower.server.domain.admin.dto;

import com.sinsaflower.server.domain.admin.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 관리자 정보 응답 DTO
 * 관리자의 기본 정보를 클라이언트에 전달하기 위한 객체
 */
@Getter @Setter
@Builder
@Schema(description = "관리자 정보 응답", example = """
    {
        "id": 1,
        "loginId": "admin",
        "name": "시스템 관리자",
        "createdAt": "2024-01-10T09:00:00",
        "lastLoginAt": "2024-01-15T15:30:00"
    }
    """)
public class AdminResponse {
    
    @Schema(description = "관리자 고유 ID", example = "1", required = true)
    private Long id;
    
    @Schema(description = "관리자 로그인 ID", example = "admin", required = true)
    private String loginId;
    
    @Schema(description = "관리자 이름", example = "시스템 관리자", required = true)
    private String name;
    
    @Schema(description = "관리자 계정 생성일시", example = "2024-01-10T09:00:00", required = true)
    private LocalDateTime createdAt;
    
    @Schema(description = "마지막 로그인 일시", example = "2024-01-15T15:30:00")
    private LocalDateTime lastLoginAt;
    
    public static AdminResponse from(Admin admin) {
        return AdminResponse.builder()
            .id(admin.getId())
            .loginId(admin.getLoginId())
            .name(admin.getName())
            .createdAt(admin.getCreatedAt())
            .lastLoginAt(admin.getLastLoginAt())
            .build();
    }
} 
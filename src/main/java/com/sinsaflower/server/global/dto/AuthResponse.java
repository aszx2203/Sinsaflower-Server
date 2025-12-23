package com.sinsaflower.server.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 인증 응답 DTO
 * 로그인 성공 시 JWT 토큰과 사용자 정보 반환
 */
@Getter @Setter
@Builder
@Schema(description = "인증 응답", example = """
    {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwYXJ0bmVyMTIzIiwiaWF0IjoxNjQzNzA3MjAwLCJleHAiOjE2NDM3MTA4MDB9.signature",
        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJwYXJ0bmVyMTIzIiwiaWF0IjoxNjQzNzA3MjAwLCJleHAiOjE2NDM3OTM2MDB9.signature", 
        "tokenType": "Bearer",
        "userId": 123,
        "username": "partner123",
        "userType": "PARTNER",
        "name": "김파트너",
        "authorities": ["ROLE_PARTNER"]
    }
    """)
public class AuthResponse {
    
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "로그인 ID", example = "admin")
    private String username;

    @Schema(description = "사용자 닉네임", example = "어드민 닉네임")
    private String nickname;
    
    @Schema(description = "사용자 타입", example = "ADMIN", allowableValues = {"ADMIN", "PARTNER"})
    private String userType;
    
    @Schema(description = "사용자 이름", example = "관리자")
    private String name;
    
    @Schema(description = "권한 목록", example = "[\"ROLE_ADMIN\"]")
    private List<String> authorities;
} 
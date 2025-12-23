package com.sinsaflower.server.domain.member.dto;

import com.sinsaflower.server.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 회원 정보 응답 DTO
 * 회원의 기본 정보를 클라이언트에 전달하기 위한 객체
 */
@Getter
@Builder
@Schema(description = "회원 정보 응답", example = """
    {
        "id": 123,
        "loginId": "partner123",
        "name": "김파트너", 
        "nickname": "신사꽃농장",
        "mobile": "010-1234-5678",
        "status": "활성",
        "createdAt": "2024-01-15T10:30:00",
        "lastLoginAt": "2024-01-15T15:30:00"
    }
    """)
public class MemberResponse {
    
    @Schema(description = "회원 고유 ID", example = "123", required = true)
    private Long id;
    
    @Schema(description = "로그인 ID", example = "partner123", required = true)
    private String loginId;
    
    @Schema(description = "실명", example = "김파트너", required = true)
    private String name;
    
    @Schema(description = "상호명/닉네임", example = "신사꽃농장", required = true)
    private String nickname;
    
    @Schema(description = "휴대전화번호", example = "010-1234-5678", required = true)
    private String mobile;
    
    @Schema(description = "계정 상태", example = "활성", required = true, 
            allowableValues = {"활성", "대기", "승인 거부", "비활성"})
    private String status;
    
    @Schema(description = "가입일시", example = "2024-01-15T10:30:00", required = true)
    private LocalDateTime createdAt;
    
    @Schema(description = "마지막 로그인 일시", example = "2024-01-15T15:30:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "사업자 정보")
    private BusinessProfileResponse businessProfile;
    
    // Entity -> DTO 변환 메서드
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
            .id(member.getId())
            .loginId(member.getLoginId())
            .name(member.getName())
            .nickname(member.getNickname())
            .mobile(member.getMobile())
            .status(member.getStatus().getDescription())
            .createdAt(member.getCreatedAt())
            .lastLoginAt(member.getLastLoginAt())
            .businessProfile(BusinessProfileResponse.from(member.getBusinessProfile()))
            .build();
    }
} 
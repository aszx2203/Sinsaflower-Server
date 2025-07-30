package com.sinsaflower.server.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 통합 로그인 요청 DTO
 * 관리자와 파트너 모두 동일한 로그인 폼 사용
 */
@Getter @Setter
@Schema(description = "로그인 요청", example = """
    {
        "loginId": "partner123",
        "password": "password123",
        "rememberMe": false
    }
    """)
public class LoginRequest {
    
    @NotBlank(message = "로그인 ID는 필수입니다.")
    @Size(min = 3, max = 50, message = "로그인 ID는 3-50자여야 합니다.")
    @Schema(description = "로그인 ID", example = "admin", required = true)
    private String loginId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 100, message = "비밀번호는 6-100자여야 합니다.")
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
    
    @Schema(description = "자동 로그인 여부", example = "false")
    private Boolean rememberMe = false;
} 
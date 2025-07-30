package com.sinsaflower.server.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 관리자 로그인 요청 DTO (deprecated)
 * 통합 로그인 API(/api/auth/login) 사용을 권장합니다.
 */
@Getter @Setter
@Schema(description = "관리자 로그인 요청 (deprecated)", example = """
    {
        "loginId": "admin",
        "password": "adminPassword123"
    }
    """)
public class AdminLoginRequest {
    
    @NotBlank(message = "로그인 ID는 필수입니다.")
    @Size(min = 3, max = 50, message = "로그인 ID는 3-50자여야 합니다.")
    @Schema(description = "관리자 로그인 ID", example = "admin", required = true)
    private String loginId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 100, message = "비밀번호는 6-100자여야 합니다.")
    @Schema(description = "관리자 비밀번호", example = "adminPassword123", required = true)
    private String password;
} 
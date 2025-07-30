package com.sinsaflower.server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "회원 로그인 요청")
public class MemberLoginRequest {
    
    @NotBlank(message = "로그인 ID는 필수입니다.")
    @Size(min = 4, max = 20, message = "로그인 ID는 4-20자여야 합니다.")
    @Schema(description = "로그인 ID", example = "partner123", required = true)
    private String loginId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 20, message = "비밀번호는 6-20자여야 합니다.")
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
    
    @Schema(description = "자동 로그인 여부", example = "false")
    private Boolean rememberMe = false;
} 
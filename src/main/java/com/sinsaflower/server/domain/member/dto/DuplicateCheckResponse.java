package com.sinsaflower.server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 중복 확인 응답 DTO
 * 로그인 ID, 전화번호, 사업자등록번호 등의 중복 확인 결과를 반환
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "중복 확인 응답", example = "{\"exists\":false,\"message\":\"사용 가능한 로그인 ID입니다.\",\"available\":true}")
public class DuplicateCheckResponse {
    
    @Schema(description = "중복 여부", example = "false", required = true)
    private final boolean exists;
    
    @Schema(description = "결과 메시지", example = "사용 가능한 로그인 ID입니다.", required = true)
    private final String message;
    
    @Schema(description = "사용 가능 여부 (exists의 반대)", example = "true", required = true)
    public boolean isAvailable() {
        return !exists;
    }
    
    // 편의 메서드들
    public static DuplicateCheckResponse available(String type) {
        return new DuplicateCheckResponse(false, String.format("사용 가능한 %s입니다.", type));
    }
    
    public static DuplicateCheckResponse duplicate(String type) {
        return new DuplicateCheckResponse(true, String.format("이미 사용 중인 %s입니다.", type));
    }
    
    public static DuplicateCheckResponse loginIdAvailable() {
        return available("로그인 ID");
    }
    
    public static DuplicateCheckResponse loginIdDuplicate() {
        return duplicate("로그인 ID");
    }
    
    public static DuplicateCheckResponse mobileAvailable() {
        return available("전화번호");
    }
    
    public static DuplicateCheckResponse mobileDuplicate() {
        return duplicate("전화번호");
    }
    
    public static DuplicateCheckResponse businessNumberAvailable() {
        return new DuplicateCheckResponse(false, "사용 가능한 사업자등록번호입니다.");
    }
    
    public static DuplicateCheckResponse businessNumberDuplicate() {
        return new DuplicateCheckResponse(true, "이미 등록된 사업자등록번호입니다.");
    }
} 
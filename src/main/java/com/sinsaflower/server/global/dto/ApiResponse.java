package com.sinsaflower.server.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 표준화된 API 응답 구조
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 응답 구조")
public class ApiResponse<T> {
    
    @Schema(description = "응답 코드", example = "200")
    private int code;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "응답 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
    
    // 성공 응답 생성 메서드들
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청이 성공적으로 처리되었습니다.", data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, LocalDateTime.now());
    }
    
    // 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "요청이 성공적으로 처리되었습니다.", null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null, LocalDateTime.now());
    }
    
    // 생성 성공 응답
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "리소스가 성공적으로 생성되었습니다.", data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data, LocalDateTime.now());
    }
    
    // 실패 응답 생성 메서드들
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null, LocalDateTime.now());
    }
    
    // 잘못된 요청
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null, LocalDateTime.now());
    }
    
    // 인증 실패
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, null, LocalDateTime.now());
    }
    
    // 권한 없음
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message, null, LocalDateTime.now());
    }
    
    // 리소스 없음
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null, LocalDateTime.now());
    }
    
    // 충돌 (중복 등)
    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409, message, null, LocalDateTime.now());
    }
    
    // 서버 오류
    public static <T> ApiResponse<T> internalServerError(String message) {
        return new ApiResponse<>(500, message, null, LocalDateTime.now());
    }
} 
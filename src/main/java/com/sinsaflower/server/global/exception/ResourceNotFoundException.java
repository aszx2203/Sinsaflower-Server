package com.sinsaflower.server.global.exception;

/**
 * 리소스를 찾을 수 없는 예외
 * HTTP 404 Not Found
 */
public class ResourceNotFoundException extends BusinessException {
    
    private static final String ERROR_CODE = "RESOURCE_001";
    
    public ResourceNotFoundException(String message) {
        super(404, ERROR_CODE, message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(404, ERROR_CODE, message, cause);
    }
    
    // 편의 메서드들
    public static ResourceNotFoundException member(Long memberId) {
        return new ResourceNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId);
    }
    
    public static ResourceNotFoundException admin(Long adminId) {
        return new ResourceNotFoundException("관리자를 찾을 수 없습니다. ID: " + adminId);
    }
    
    public static ResourceNotFoundException memberByLoginId(String loginId) {
        return new ResourceNotFoundException("회원을 찾을 수 없습니다. 로그인 ID: " + loginId);
    }
    
    public static ResourceNotFoundException businessProfile(Long memberId) {
        return new ResourceNotFoundException("사업자 정보를 찾을 수 없습니다. 회원 ID: " + memberId);
    }
} 
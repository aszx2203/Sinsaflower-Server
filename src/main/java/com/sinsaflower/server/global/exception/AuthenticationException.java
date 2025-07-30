package com.sinsaflower.server.global.exception;

/**
 * 인증 실패 예외
 * HTTP 401 Unauthorized
 */
public class AuthenticationException extends BusinessException {
    
    private static final String ERROR_CODE = "AUTH_001";
    
    public AuthenticationException(String message) {
        super(401, ERROR_CODE, message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(401, ERROR_CODE, message, cause);
    }
    
    // 편의 메서드들
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("로그인 ID 또는 비밀번호가 올바르지 않습니다.");
    }
    
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("토큰이 만료되었습니다. 다시 로그인해주세요.");
    }
    
    public static AuthenticationException invalidToken() {
        return new AuthenticationException("유효하지 않은 토큰입니다.");
    }
    
    public static AuthenticationException accountInactive() {
        return new AuthenticationException("승인되지 않은 계정입니다. 관리자 승인 후 이용 가능합니다.");
    }
} 
package com.sinsaflower.server.global.exception;

/**
 * 중복 리소스 예외
 * HTTP 409 Conflict
 */
public class DuplicateResourceException extends BusinessException {
    
    private static final String ERROR_CODE = "DUPLICATE_001";
    
    public DuplicateResourceException(String message) {
        super(409, ERROR_CODE, message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(409, ERROR_CODE, message, cause);
    }
    
    // 편의 메서드들
    public static DuplicateResourceException loginId(String loginId) {
        return new DuplicateResourceException("이미 사용 중인 로그인 ID입니다: " + loginId);
    }
    
    public static DuplicateResourceException mobile(String mobile) {
        return new DuplicateResourceException("이미 사용 중인 전화번호입니다: " + mobile);
    }
    
    public static DuplicateResourceException businessNumber(String businessNumber) {
        return new DuplicateResourceException("이미 사용 중인 사업자등록번호입니다: " + businessNumber);
    }
    
    public static DuplicateResourceException nickname(String nickname) {
        return new DuplicateResourceException("이미 사용 중인 닉네임입니다: " + nickname);
    }
} 
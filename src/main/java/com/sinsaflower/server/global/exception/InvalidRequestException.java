package com.sinsaflower.server.global.exception;

/**
 * 잘못된 요청 예외
 * HTTP 400 Bad Request
 */
public class InvalidRequestException extends BusinessException {
    
    private static final String ERROR_CODE = "REQUEST_001";
    
    public InvalidRequestException(String message) {
        super(400, ERROR_CODE, message);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(400, ERROR_CODE, message, cause);
    }
    
    // 편의 메서드들
    public static InvalidRequestException missingParameter(String parameterName) {
        return new InvalidRequestException("필수 파라미터가 누락되었습니다: " + parameterName);
    }
    
    public static InvalidRequestException invalidParameter(String parameterName, String reason) {
        return new InvalidRequestException("잘못된 파라미터입니다. " + parameterName + ": " + reason);
    }
    
    public static InvalidRequestException invalidFileType(String allowedTypes) {
        return new InvalidRequestException("허용되지 않는 파일 형식입니다. 허용 형식: " + allowedTypes);
    }
    
    public static InvalidRequestException fileTooLarge(String maxSize) {
        return new InvalidRequestException("파일 크기가 제한을 초과했습니다. 최대 크기: " + maxSize);
    }
    
    public static InvalidRequestException invalidStatus(String currentStatus, String requestedStatus) {
        return new InvalidRequestException("현재 상태에서는 요청된 작업을 수행할 수 없습니다. 현재: " + currentStatus + ", 요청: " + requestedStatus);
    }
} 
package com.sinsaflower.server.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 관련 기본 예외 클래스
 */
@Getter
public abstract class BusinessException extends RuntimeException {
    
    private final int status;
    private final String code;
    
    protected BusinessException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
    
    protected BusinessException(int status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }
} 
package com.sinsaflower.server.global.exception;

import com.sinsaflower.server.global.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * 모든 예외를 통합적으로 처리하여 일관된 API 응답 제공
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business Exception: [{}] {}", ex.getCode(), ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * 유효성 검증 실패 (@Valid 어노테이션)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation Exception: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
            
        ApiResponse<Void> response = ApiResponse.badRequest("입력값 검증에 실패했습니다. " + errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 바인딩 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        log.warn("Bind Exception: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
            
        ApiResponse<Void> response = ApiResponse.badRequest("입력값 바인딩에 실패했습니다. " + errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 제약조건 위반 예외
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint Violation Exception: {}", ex.getMessage());
        
        String errorMessage = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
            
        ApiResponse<Void> response = ApiResponse.badRequest("제약조건 위반: " + errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 필수 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(MissingServletRequestParameterException ex) {
        log.warn("Missing Parameter Exception: {}", ex.getMessage());
        
        String message = String.format("필수 파라미터가 누락되었습니다: %s (%s)", ex.getParameterName(), ex.getParameterType());
        ApiResponse<Void> response = ApiResponse.badRequest(message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 파라미터 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Type Mismatch Exception: {}", ex.getMessage());
        
        String message = String.format("파라미터 타입이 올바르지 않습니다: %s (요구 타입: %s)", 
            ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        ApiResponse<Void> response = ApiResponse.badRequest(message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * HTTP 메서드 지원하지 않음
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method Not Supported Exception: {}", ex.getMessage());
        
        String message = String.format("지원하지 않는 HTTP 메서드입니다: %s (지원 메서드: %s)", 
            ex.getMethod(), String.join(", ", ex.getSupportedMethods() != null ? ex.getSupportedMethods() : new String[0]));
        ApiResponse<Void> response = ApiResponse.error(405, message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * JSON 파싱 오류
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Message Not Readable Exception: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.badRequest("요청 본문을 파싱할 수 없습니다. JSON 형식을 확인해주세요.");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 파일 업로드 크기 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeException(MaxUploadSizeExceededException ex) {
        log.warn("Max Upload Size Exceeded: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.badRequest("업로드 파일 크기가 제한을 초과했습니다.");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 접근 권한 없음 (Spring Security)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied Exception: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.forbidden("접근 권한이 없습니다.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * IllegalArgumentException 처리 (기존 코드 호환성)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal Argument Exception: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.badRequest(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 일반적인 런타임 예외
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime Exception: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.internalServerError("서버 내부 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 예상하지 못한 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unexpected Exception: {}", ex.getMessage(), ex);
        
        ApiResponse<Void> response = ApiResponse.internalServerError("예상하지 못한 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(response);
    }
} 
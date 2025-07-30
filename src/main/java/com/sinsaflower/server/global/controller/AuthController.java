package com.sinsaflower.server.global.controller;

import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.global.dto.AuthResponse;
import com.sinsaflower.server.global.dto.LoginRequest;
import com.sinsaflower.server.global.dto.TokenRefreshRequest;
import com.sinsaflower.server.global.security.CustomUserDetails;
import com.sinsaflower.server.global.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 인증 API 컨트롤러
 * 관리자와 파트너 통합 인증 처리
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 API", description = "로그인, 로그아웃, 토큰 관리 API")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 파트너 회원가입
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "파트너 회원가입", 
        description = "새로운 파트너 회원을 등록합니다. multipart/form-data 형식으로 JSON 데이터와 파일을 함께 전송합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "중복된 정보")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> signup(
        @RequestPart("request") 
        @Schema(description = "회원가입 정보 (JSON)", implementation = MemberSignupRequest.class)
        @Valid MemberSignupRequest request,
        
        @RequestPart(value = "businessCertFile", required = false)
        @Schema(description = "사업자등록증 파일 (PDF, JPG, PNG)", type = "string", format = "binary")
        MultipartFile businessCertFile,
        
        @RequestPart(value = "bankCertFile", required = false)
        @Schema(description = "통장사본 파일 (PDF, JPG, PNG)", type = "string", format = "binary")
        MultipartFile bankCertFile
    ) {
        log.info("파트너 회원가입 요청: {}", request.getLoginId());

        if (request.getBusinessProfile() != null) {
            request.getBusinessProfile().setBusinessCertFile(businessCertFile);
            request.getBusinessProfile().setBankCertFile(bankCertFile);
        }

        MemberResponse response = authService.signUp(request);
        log.info("회원가입 성공: {} (ID: {})", response.getLoginId(), response.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(com.sinsaflower.server.global.dto.ApiResponse.created("회원가입이 성공적으로 완료되었습니다.", response));
    }
    /**
     * 통합 로그인
     */
    @PostMapping("/login")
    @Operation(
        summary = "통합 로그인", 
        description = "관리자와 파트너 모두 사용할 수 있는 통합 로그인 API입니다. 로그인 성공 시 JWT 토큰을 발급합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공",
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "로그인 실패")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<AuthResponse>> login(
        @RequestBody
        @Schema(description = "로그인 요청 정보", implementation = LoginRequest.class)
        @Valid LoginRequest request) {
        log.info("로그인 API 호출: {}", request.getLoginId());
        
        AuthResponse response = authService.login(request);
        log.info("로그인 성공: {} ({})", response.getUsername(), response.getUserType());
        
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("로그인이 성공적으로 완료되었습니다.", response));
    }
    
    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 액세스 토큰 발급")
    @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("토큰 갱신 API 호출");
        
        try {
            AuthResponse response = authService.refreshToken(request);
            log.info("토큰 갱신 성공: {} ({})", response.getUsername(), response.getUserType());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 세션 무효화")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("로그아웃 API 호출: {}", userDetails != null ? userDetails.getUsername() : "익명");
        
        authService.logout();
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("로그아웃되었습니다."));
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보 조회", description = "인증된 사용자의 정보 반환")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Map<String, Object>>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("현재 사용자 정보 조회 API 호출: {}", userDetails != null ? userDetails.getUsername() : "익명");
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(com.sinsaflower.server.global.dto.ApiResponse.unauthorized("인증되지 않은 사용자입니다."));
        }
        
        Map<String, Object> userInfo = Map.of(
            "userId", userDetails.getUserId(),
            "username", userDetails.getUsername(),
            "userType", userDetails.getUserType(),
            "name", userDetails.getName(),
            "authorities", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList(),
            "isAdmin", userDetails.isAdmin(),
            "isPartner", userDetails.isPartner()
        );
        
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("사용자 정보 조회가 성공적으로 완료되었습니다.", userInfo));
    }
    
    /**
     * 토큰 유효성 검증
     */
    @PostMapping("/validate")
    @Operation(summary = "토큰 유효성 검증", description = "JWT 토큰의 유효성을 검증")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 유효"),
        @ApiResponse(responseCode = "401", description = "토큰 무효")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Map<String, Object>>> validateToken(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("토큰 유효성 검증 API 호출: {}", userDetails != null ? userDetails.getUsername() : "익명");
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(com.sinsaflower.server.global.dto.ApiResponse.unauthorized("유효하지 않은 토큰입니다."));
        }
        
        Map<String, Object> tokenInfo = Map.of(
            "valid", true,
            "username", userDetails.getUsername(),
            "userType", userDetails.getUserType(),
            "authorities", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList()
        );
        
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("토큰이 유효합니다.", tokenInfo));
    }
} 
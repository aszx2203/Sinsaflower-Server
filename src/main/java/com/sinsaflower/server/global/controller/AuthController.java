package com.sinsaflower.server.global.controller;

import com.sinsaflower.server.domain.admin.repository.AdminRepository;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import com.sinsaflower.server.global.constants.AuthConstants;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    

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
        @Valid LoginRequest request,
        HttpServletResponse httpResponse) {
        log.info("로그인 API 호출: {}", request.getLoginId());
        log.info("로그인 API 호출: {}", request.getPassword());

        AuthResponse response = authService.login(request);
        log.info("로그인 성공: {} ({})", response.getUsername(), response.getUserType());
        
        // JWT 토큰을 쿠키에 설정 (HttpOnly - 보안)
        setTokenCookie(httpResponse, AuthConstants.Token.ACCESS_TOKEN_COOKIE, response.getAccessToken(), 
                      AuthConstants.Token.ACCESS_TOKEN_EXPIRES_SECONDS, AuthConstants.Cookie.HTTP_ONLY_TOKEN);
        if (response.getRefreshToken() != null) {
            setTokenCookie(httpResponse, AuthConstants.Token.REFRESH_TOKEN_COOKIE, response.getRefreshToken(), 
                          AuthConstants.Token.REFRESH_TOKEN_EXPIRES_SECONDS, AuthConstants.Cookie.HTTP_ONLY_TOKEN);
        }
        
        setStatusCookie(httpResponse, AuthConstants.Cookie.IS_LOGGED_IN, "true", AuthConstants.Token.ACCESS_TOKEN_EXPIRES_SECONDS);
        setStatusCookie(httpResponse, AuthConstants.Cookie.USER_TYPE, response.getUserType(), AuthConstants.Token.ACCESS_TOKEN_EXPIRES_SECONDS);
        setStatusCookie(httpResponse, AuthConstants.Cookie.USERNAME, response.getUsername(), AuthConstants.Token.ACCESS_TOKEN_EXPIRES_SECONDS);
        
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(AuthConstants.Messages.LOGIN_SUCCESS, response));
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
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Void>> logout(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletResponse httpResponse) {
        log.info("로그아웃 API 호출: {}", userDetails != null ? userDetails.getUsername() : "익명");
        
        authService.logout();
        
        // 쿠키에서 토큰 제거
        clearTokenCookie(httpResponse, AuthConstants.Token.ACCESS_TOKEN_COOKIE);
        clearTokenCookie(httpResponse, AuthConstants.Token.REFRESH_TOKEN_COOKIE);
        
        // 상태 쿠키도 제거
        clearStatusCookie(httpResponse, AuthConstants.Cookie.IS_LOGGED_IN);
        clearStatusCookie(httpResponse, AuthConstants.Cookie.USER_TYPE);
        clearStatusCookie(httpResponse, AuthConstants.Cookie.USERNAME);
        
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(AuthConstants.Messages.LOGOUT_SUCCESS));
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
                .body(com.sinsaflower.server.global.dto.ApiResponse.unauthorized(AuthConstants.Messages.UNAUTHORIZED_USER));
        }

        // Map.of()는 불변이므로 수정 가능한 HashMap 사용
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userDetails.getUserId());
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("userType", userDetails.getUserType());
        userInfo.put("authorities", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList());
        userInfo.put("isAdmin", userDetails.isAdmin());
        userInfo.put("isPartner", userDetails.isPartner());

        // DB에서 최신 name, nickname 정보 조회
        if (userDetails.isAdmin()) {
            adminRepository.findById(userDetails.getUserId()).ifPresent(admin -> {
                userInfo.put("name", admin.getName());
                userInfo.put("nickname", admin.getName()); // 관리자는 name을 nickname으로 사용
            });
        } else if (userDetails.isPartner()) {
            memberRepository.findById(userDetails.getUserId()).ifPresent(member -> {
                userInfo.put("name", member.getName());
                userInfo.put("nickname", member.getNickname());
            });
        }
        
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(AuthConstants.Messages.USER_INFO_SUCCESS, userInfo));
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
                .body(com.sinsaflower.server.global.dto.ApiResponse.unauthorized(AuthConstants.Messages.INVALID_TOKEN));
        }
        
        Map<String, Object> tokenInfo = Map.of(
            "valid", true,
            "username", userDetails.getUsername(),
            "userType", userDetails.getUserType(),
            "authorities", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList()
        );
        
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(AuthConstants.Messages.TOKEN_VALID, tokenInfo));
    }
    
    /**
     * 토큰을 쿠키에 설정하는 헬퍼 메서드 (HttpOnly 설정 가능)
     */
    private void setTokenCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds, boolean httpOnly) {
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(String.format("%s=%s; Path=%s; Max-Age=%d; SameSite=%s", 
                           name, value, AuthConstants.Cookie.COOKIE_PATH, maxAgeInSeconds, AuthConstants.Cookie.SAME_SITE));
        
        if (httpOnly) {
            cookieHeader.append("; HttpOnly"); // 보안 토큰용
        }
        
        // 프로덕션에서는 Secure 추가 (현재는 개발환경)
        // cookieHeader.append("; Secure");
        
        response.addHeader("Set-Cookie", cookieHeader.toString());
        
        log.debug("토큰 쿠키 설정: {} (HttpOnly: {}, 만료: {}초)", name, httpOnly, maxAgeInSeconds);
    }
    
    /**
     * 상태 쿠키 설정 (프론트엔드 접근 가능)
     */
    private void setStatusCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
        setTokenCookie(response, name, value, maxAgeInSeconds, AuthConstants.Cookie.HTTP_ONLY_STATUS);
    }
    
    /**
     * 토큰 쿠키 제거 (HttpOnly)
     */
    private void clearTokenCookie(HttpServletResponse response, String name) {
        String cookieHeader = String.format("%s=; Path=%s; Max-Age=0; HttpOnly; SameSite=%s", 
                                           name, AuthConstants.Cookie.COOKIE_PATH, AuthConstants.Cookie.SAME_SITE);
        response.addHeader("Set-Cookie", cookieHeader);
        log.debug("토큰 쿠키 제거: {}", name);
    }
    
    /**
     * 상태 쿠키 제거 (일반)
     */
    private void clearStatusCookie(HttpServletResponse response, String name) {
        String cookieHeader = String.format("%s=; Path=%s; Max-Age=0; SameSite=%s", 
                                           name, AuthConstants.Cookie.COOKIE_PATH, AuthConstants.Cookie.SAME_SITE);
        response.addHeader("Set-Cookie", cookieHeader);
        log.debug("상태 쿠키 제거: {}", name);
    }
} 
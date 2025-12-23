package com.sinsaflower.server.global.security;

import com.sinsaflower.server.global.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

/**
 * JWT 토큰을 검증하고 인증 정보를 설정하는 필터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JWT_COOKIE_NAME = "accessToken"; // 쿠키명 정의
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // JWT 토큰 추출
            String jwt = extractJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                // 토큰에서 사용자 정보 추출
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                String username = jwtUtil.getUsernameFromToken(jwt);
                String userType = jwtUtil.getUserTypeFromToken(jwt);
                Collection<? extends GrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(jwt);
                
                // CustomUserDetails 생성
                CustomUserDetails userDetails = new CustomUserDetails(userId, username, userType, authorities);
                
                // 인증 토큰 생성
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("JWT 인증 성공 - 사용자: {}, 타입: {}, 권한: {}", 
                    username, userType, authorities);
            }
            
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            // 인증 실패 시 SecurityContext 초기화
            SecurityContextHolder.clearContext();
        }
        
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
    
    /**
     * HTTP 요청에서 JWT 토큰 추출 (Header 또는 Cookie에서)
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        log.info("extractJwtFromRequest method");
        // 1. Authorization Header에서 토큰 추출 시도
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX) && !bearerToken.substring(BEARER_PREFIX.length()).equalsIgnoreCase("undefined")) {
            log.debug("JWT 토큰을 Authorization Header에서 추출");
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        // 2. Cookie에서 토큰 추출 시도
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    String cookieValue = cookie.getValue();
                    if (StringUtils.hasText(cookieValue) && !cookieValue.equalsIgnoreCase("undefined")) {
                        log.debug("JWT 토큰을 Cookie에서 추출");
                        return cookieValue;
                    }
                }
            }
        }
        
        log.debug("JWT 토큰을 찾을 수 없음 (Header 및 Cookie 모두 확인)");
        return null;
    }
    
    /**
     * 필터를 적용하지 않을 경로 설정
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("인증 경로 {}", path);
        if (path.startsWith("/api/auth/me")) {
            return false;
        }
        // 인증이 필요없는 경로들
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/members/validation/") ||
               path.startsWith("/api/members/signup") ||
               path.startsWith("/actuator/") ||
               // SpringDoc OpenAPI 2.8.0 호환 경로들
               path.startsWith("/swagger-ui/") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/api-docs/") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/webjars/") ||
               path.equals("/h2-console") ||
               path.equals("/login") ||
               path.equals("/");
    }
} 
package com.sinsaflower.server.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 토큰 유틸리티 클래스
 * - 토큰 생성, 검증, 정보 추출 기능 제공
 */
@Component
@Slf4j
public class JwtUtil {
    
    private final SecretKey key;
    private final int jwtExpiration;
    private final int refreshExpiration;
    
    // JWT 클레임 키 상수
    private static final String AUTHORITIES_KEY = "auth";
    private static final String USER_TYPE_KEY = "userType";
    private static final String USER_ID_KEY = "userId";
    
    public JwtUtil(
        @Value("${jwt.secret}") String jwtSecret,
        @Value("${jwt.expiration}") int jwtExpiration,
        @Value("${jwt.refresh-expiration}") int refreshExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }
    
    /**
     * Access Token 생성
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities, String userType, Long userId) {
        return generateToken(username, authorities, userType, userId, jwtExpiration);
    }
    
    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String username, Collection<? extends GrantedAuthority> authorities, String userType, Long userId) {
        return generateToken(username, authorities, userType, userId, refreshExpiration);
    }
    
    /**
     * JWT 토큰 생성 공통 메서드
     */
    private String generateToken(String username, Collection<? extends GrantedAuthority> authorities, String userType, Long userId, int expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        // 권한 목록을 문자열로 변환
        String authoritiesString = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
        
        return Jwts.builder()
            .subject(username)
            .claim(AUTHORITIES_KEY, authoritiesString)
            .claim(USER_TYPE_KEY, userType)
            .claim(USER_ID_KEY, userId)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }
    
    /**
     * JWT 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    /**
     * JWT 토큰에서 권한 정보 추출
     */
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String authoritiesString = claims.get(AUTHORITIES_KEY, String.class);
        
        if (authoritiesString == null || authoritiesString.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(authoritiesString.split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
    
    /**
     * JWT 토큰에서 사용자 타입 추출
     */
    public String getUserTypeFromToken(String token) {
        return getClaimsFromToken(token).get(USER_TYPE_KEY, String.class);
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get(USER_ID_KEY, Long.class);
    }
    
    /**
     * JWT 토큰에서 만료 시간 추출
     */
    public Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }
    
    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        log.info("검증할 토큰: '{}'", token); // 전달된 토큰
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * JWT 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("JWT 토큰 만료 시간 확인 실패: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * JWT 토큰에서 Claims 추출
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰에서 Claims 추출 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.", e);
        }
    }
    
    /**
     * 토큰에서 Bearer 접두사 제거
     */
    public String extractTokenFromBearer(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }
} 
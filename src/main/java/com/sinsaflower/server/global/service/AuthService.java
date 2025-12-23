package com.sinsaflower.server.global.service;

import com.sinsaflower.server.domain.admin.entity.Admin;
import com.sinsaflower.server.domain.admin.repository.AdminRepository;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import com.sinsaflower.server.domain.member.service.MemberService;
import com.sinsaflower.server.global.dto.AuthResponse;
import com.sinsaflower.server.global.dto.LoginRequest;
import com.sinsaflower.server.global.dto.TokenRefreshRequest;
import com.sinsaflower.server.global.jwt.JwtUtil;
import com.sinsaflower.server.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 관리자와 파트너 로그인을 통합 처리하는 인증 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 파트너 회원가입
     */
    @Transactional
    public MemberResponse signUp(MemberSignupRequest request) {
        log.info("파트너 회원가입 처리: {}", request.getLoginId());
        
        // MemberService의 회원가입 로직 활용
        return memberService.signUp(request);
    }
    
    /**
     * 통합 로그인 처리
     * 관리자와 파트너 모두 동일한 loginId로 로그인 시도
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("로그인 시도: {}", request.getLoginId());
        
        // 1. 관리자 로그인 시도
        Optional<Admin> adminOpt = adminRepository.findByLoginId(request.getLoginId());
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                // 관리자 로그인 성공
                admin.updateLastLogin();
                adminRepository.save(admin);
                
                CustomUserDetails userDetails = new CustomUserDetails(admin);
                return generateTokenResponse(userDetails);
            }
        }
        
        // 2. 파트너 로그인 시도
        Optional<Member> memberOpt = memberRepository.findByLoginId(request.getLoginId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            if (passwordEncoder.matches(request.getPassword(), member.getPassword())) {
                // 파트너 계정 상태 확인
                if (!Member.MemberStatus.ACTIVE.equals(member.getStatus())) {
                    log.warn("비활성 파트너 로그인 시도: {} (상태: {})", request.getLoginId(), member.getStatus());
                    throw new IllegalArgumentException("승인되지 않은 계정입니다. 관리자 승인 후 이용 가능합니다.");
                }
                
                // 파트너 로그인 성공
                member.updateLastLogin();
                memberRepository.save(member);
                
                CustomUserDetails userDetails = new CustomUserDetails(member);
                return generateTokenResponse(userDetails);
            }
        }
        
        // 3. 로그인 실패
        log.warn("로그인 실패: {}", request.getLoginId());
        throw new IllegalArgumentException("로그인 ID 또는 비밀번호가 올바르지 않습니다.");
    }
    
    /**
     * 토큰 갱신
     */
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // 리프레시 토큰 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            log.error("유효하지 않은 리프레시 토큰");
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
        
        // 토큰에서 사용자 정보 추출
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String userType = jwtUtil.getUserTypeFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        
        // 사용자 존재 여부 확인
        if (CustomUserDetails.USER_TYPE_ADMIN.equals(userType)) {
            adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));
        } else if (CustomUserDetails.USER_TYPE_PARTNER.equals(userType)) {
            Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파트너입니다."));
            
            // 파트너 계정 상태 확인
            if (!Member.MemberStatus.ACTIVE.equals(member.getStatus())) {
                throw new IllegalArgumentException("비활성 계정입니다.");
            }
        }
        
        // 새 토큰 생성
        CustomUserDetails userDetails = new CustomUserDetails(
            userId, username, userType, jwtUtil.getAuthoritiesFromToken(refreshToken)
        );
        
        log.info("토큰 갱신 성공: {} ({})", username, userType);
        return generateTokenResponse(userDetails);
    }
    
    /**
     * 로그아웃 처리
     * JWT는 stateless이므로 서버에서 특별한 처리 없이 클라이언트에서 토큰 삭제
     */
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("로그아웃: {} ({})", userDetails.getUsername(), userDetails.getUserType());
        }
        
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 현재 로그인 사용자 정보 조회
     */
    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        throw new IllegalArgumentException("로그인이 필요합니다.");
    }
    
    /**
     * JWT 토큰 응답 생성
     */
    private AuthResponse generateTokenResponse(CustomUserDetails userDetails) {
        String accessToken = jwtUtil.generateToken(
            userDetails.getUsername(),
            userDetails.getAuthorities(),
            userDetails.getUserType(),
            userDetails.getUserId()
        );
        
        String refreshToken = jwtUtil.generateRefreshToken(
            userDetails.getUsername(),
            userDetails.getAuthorities(),
            userDetails.getUserType(),
            userDetails.getUserId()
        );
        
        log.info("토큰 생성 성공: {} ({})", userDetails.getUsername(), userDetails.getUserType());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .userId(userDetails.getUserId())
            .username(userDetails.getUsername())
            .userType(userDetails.getUserType())
            .nickname(userDetails.getNickname())
            .name(userDetails.getName())
            .authorities(userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList())
            .build();
    }
} 
package com.sinsaflower.server.domain.admin.service;

import com.sinsaflower.server.domain.admin.dto.AdminLoginRequest;
import com.sinsaflower.server.domain.admin.dto.AdminResponse;
import com.sinsaflower.server.domain.admin.entity.Admin;
import com.sinsaflower.server.domain.admin.repository.AdminRepository;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.entity.MemberBusinessProfile;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import com.sinsaflower.server.domain.member.repository.MemberBusinessProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final MemberBusinessProfileRepository memberBusinessProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자 로그인
     */
    public Optional<AdminResponse> login(AdminLoginRequest request) {
        log.info("관리자 로그인 시도: {}", request.getLoginId());
        
        Optional<Admin> adminOpt = adminRepository.findByLoginId(request.getLoginId());
        if (adminOpt.isEmpty()) {
            log.warn("존재하지 않는 관리자 ID: {}", request.getLoginId());
            return Optional.empty();
        }
        
        Admin admin = adminOpt.get();
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            log.warn("관리자 비밀번호 불일치: {}", request.getLoginId());
            return Optional.empty();
        }
        
        // 관리자는 항상 활성 상태로 간주
        
        // 로그인 시간 업데이트
        admin.updateLastLogin();
        adminRepository.save(admin);
        
        log.info("관리자 로그인 성공: {}", request.getLoginId());
        return Optional.of(AdminResponse.from(admin));
    }

    /**
     * 관리자 정보 조회
     */
    public Optional<AdminResponse> findById(Long adminId) {
        return adminRepository.findById(adminId)
            .map(AdminResponse::from);
    }

    /**
     * 파트너 승인 (관리자 기능)
     */
    @Transactional
    public MemberResponse approvePartner(Long partnerId) {
        log.info("파트너 승인 처리 시작: {}", partnerId);

        // 파트너 회원 조회
        Member member = memberRepository.findById(partnerId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 회원 상태를 활성화로 변경
        member.setStatus(Member.MemberStatus.ACTIVE);

        // 파트너 상세 정보 승인 처리
        MemberBusinessProfile businessProfile = memberBusinessProfileRepository.findByMemberId(partnerId)
            .orElseThrow(() -> new IllegalArgumentException("파트너 상세 정보를 찾을 수 없습니다."));

        businessProfile.approve("관리자"); // 관리자 1명이므로 고정값

        log.info("파트너 승인 처리 완료: {}", member.getLoginId());
        return MemberResponse.from(member);
    }

    /**
     * 파트너 거부 (관리자 기능)
     */
    @Transactional
    public MemberResponse rejectPartner(Long partnerId, String reason) {
        log.info("파트너 승인 거부 처리 시작: {}", partnerId);

        // 파트너 회원 조회
        Member member = memberRepository.findById(partnerId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 파트너 상세 정보 거부 처리
        MemberBusinessProfile businessProfile = memberBusinessProfileRepository.findByMemberId(partnerId)
            .orElseThrow(() -> new IllegalArgumentException("파트너 상세 정보를 찾을 수 없습니다."));

        businessProfile.reject(reason);

        log.info("파트너 승인 거부 처리 완료: {}", member.getLoginId());
        return MemberResponse.from(member);
    }

    /**
     * 초기 관리자 생성 (개발용)
     */
    @Transactional
    public AdminResponse createInitialAdmin(String loginId, String password, String name) {
        // 이미 관리자가 있는지 확인
        if (adminRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 존재하는 관리자 ID입니다.");
        }
        
        Admin admin = Admin.builder()
            .loginId(loginId)
            .password(password)
            .name(name)
            .build();
        
        // 비밀번호 암호화
        admin.encodePassword(passwordEncoder);
        
        Admin savedAdmin = adminRepository.save(admin);
        
        log.info("초기 관리자 생성 완료: {}", loginId);
        return AdminResponse.from(savedAdmin);
    }
} 
package com.sinsaflower.server.domain.admin.service;

import com.sinsaflower.server.domain.admin.dto.AdminLoginRequest;
import com.sinsaflower.server.domain.admin.dto.AdminResponse;
import com.sinsaflower.server.domain.admin.entity.Admin;
import com.sinsaflower.server.domain.admin.repository.AdminRepository;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.entity.Member.MemberStatus;
import com.sinsaflower.server.domain.member.entity.MemberBusinessProfile;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import com.sinsaflower.server.domain.member.repository.MemberBusinessProfileRepository;
import com.sinsaflower.server.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
        
        Optional<Admin> adminOpt = adminRepository.findByLoginIdAndStatus(
            request.getLoginId(), Admin.AdminStatus.ACTIVE);
        
        if (adminOpt.isEmpty()) {
            log.warn("존재하지 않거나 비활성화된 관리자 ID: {}", request.getLoginId());
            return Optional.empty();
        }
        
        Admin admin = adminOpt.get();
        
        // 삭제된 관리자 확인
        if (admin.getIsDeleted()) {
            log.warn("삭제된 관리자 로그인 시도: {}", request.getLoginId());
            return Optional.empty();
        }
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            log.warn("관리자 비밀번호 불일치: {}", request.getLoginId());
            return Optional.empty();
        }
        
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
            .filter(admin -> !admin.getIsDeleted())
            .map(AdminResponse::from);
    }

    /**
     * 모든 활성 관리자 조회
     */
    public List<AdminResponse> getAllActiveAdmins() {
        return adminRepository.findAllActive().stream()
            .map(AdminResponse::from)
            .toList();
    }

    /**
     * 관리자 생성
     */
    @Transactional
    public AdminResponse createAdmin(String loginId, String password, String name, String role) {
        log.info("관리자 생성 요청: {}", loginId);

        // 중복 확인
        if (adminRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 사용 중인 로그인 ID입니다: " + loginId);
        }

        Admin admin = Admin.builder()
            .loginId(loginId)
            .password(password)
            .name(name)
            .status(Admin.AdminStatus.ACTIVE)
            .build();

        admin.encodePassword(passwordEncoder);
        Admin savedAdmin = adminRepository.save(admin);

        log.info("관리자 생성 완료: {} (ID: {})", savedAdmin.getLoginId(), savedAdmin.getId());
        return AdminResponse.from(savedAdmin);
    }

    /**
     * 관리자 상태 변경
     */
    @Transactional
    public AdminResponse updateAdminStatus(Long adminId, Admin.AdminStatus status) {
        log.info("관리자 상태 변경 요청: {} -> {}", adminId, status);

        Admin admin = adminRepository.findById(adminId)
            .filter(a -> !a.getIsDeleted())
            .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다: " + adminId));

        admin.setStatus(status);
        Admin updatedAdmin = adminRepository.save(admin);

        log.info("관리자 상태 변경 완료: {} -> {}", adminId, status);
        return AdminResponse.from(updatedAdmin);
    }

    /**
     * 관리자 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteAdmin(Long adminId, String deletedBy) {
        log.info("관리자 삭제 요청: {} by {}", adminId, deletedBy);

        Admin admin = adminRepository.findById(adminId)
            .filter(a -> !a.getIsDeleted())
            .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다: " + adminId));

        admin.softDelete(deletedBy);
        adminRepository.save(admin);

        log.info("관리자 삭제 완료: {}", adminId);
    }

    /**
     * 관리자 통계 조회
     */
    public Map<String, Long> getAdminStatistics() {
        return Map.of(
            "active", adminRepository.countByStatus(Admin.AdminStatus.ACTIVE),
            "inactive", adminRepository.countByStatus(Admin.AdminStatus.INACTIVE),
            "suspended", adminRepository.countByStatus(Admin.AdminStatus.SUSPENDED)
        );
    }

    /**
     * 멤버 승인 (관리자 기능)
     */
    @Transactional
    public MemberResponse approveMember(Long memberId) {
        log.info("멤버 승인 처리 시작: {}", memberId);

        // 멤버 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 회원 상태를 활성화로 변경
        member.setStatus(Member.MemberStatus.ACTIVE);

        // 멤버 상세 정보 승인 처리
        MemberBusinessProfile businessProfile = memberBusinessProfileRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException("멤버 상세 정보를 찾을 수 없습니다."));

        businessProfile.approve("관리자"); // 관리자 1명이므로 고정값

        log.info("멤버 승인 처리 완료: {}", member.getLoginId());
        return MemberResponse.from(member);
    }

    /**
     * 멤버 거부 (관리자 기능)
     */
    @Transactional
    public MemberResponse rejectMember(Long memberId, String reason) {
        log.info("멤버 승인 거부 처리 시작: {}", memberId);

        // 멤버 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 멤버 상세 정보 거부 처리
        MemberBusinessProfile businessProfile = memberBusinessProfileRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException("멤버 상세 정보를 찾을 수 없습니다."));

        businessProfile.reject(reason);

        log.info("멤버 승인 거부 처리 완료: {}", member.getLoginId());
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

    // ================================
    // 회원 관리 기능 (기존에 없던 것만)
    // ================================

    /**
     * 회원 정지
     */
    @Transactional
    public MemberResponse suspendMember(Long memberId) {
        log.info("회원 정지 요청: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        member.suspend();
        Member suspendedMember = memberRepository.save(member);

        log.info("회원 정지 완료: {}", memberId);
        return MemberResponse.from(suspendedMember);
    }

    /**
     * 회원 정지 해제
     */
    @Transactional
    public MemberResponse unsuspendMember(Long memberId) {
        log.info("회원 정지 해제 요청: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        member.unsuspend();
        Member unsuspendedMember = memberRepository.save(member);

        log.info("회원 정지 해제 완료: {}", memberId);
        return MemberResponse.from(unsuspendedMember);
    }

    /**
     * 회원 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteMember(Long memberId, String deletedBy) {
        log.info("회원 삭제 요청: {} by {}", memberId, deletedBy);

        Member member = memberRepository.findById(memberId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        member.softDelete(deletedBy);
        memberRepository.save(member);

        log.info("회원 삭제 완료: {}", memberId);
    }

    /**
     * 회원 통계 조회
     */
    public Map<String, Long> getMemberStatistics() {
        return Map.of(
                "pending", memberRepository.countByStatus(MemberStatus.PENDING),
                "active", memberRepository.countByStatus(MemberStatus.ACTIVE),
                "suspended", memberRepository.countByStatus(MemberStatus.SUSPENDED),
                "total", memberRepository.count() - memberRepository.countByStatus(MemberStatus.DELETED)
        );
    }
} 
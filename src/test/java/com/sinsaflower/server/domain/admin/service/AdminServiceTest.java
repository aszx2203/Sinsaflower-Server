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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService 단위 테스트")
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberBusinessProfileRepository memberBusinessProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Admin mockAdmin;
    private AdminLoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        mockAdmin = createMockAdmin();
        validLoginRequest = createValidLoginRequest();
    }

    @Test
    @DisplayName("🔴 RED: 관리자 로그인 성공 테스트")
    void login_Success() {
        // given
        given(adminRepository.findByLoginId(validLoginRequest.getLoginId()))
                .willReturn(Optional.of(mockAdmin));
        given(passwordEncoder.matches(validLoginRequest.getPassword(), mockAdmin.getPassword()))
                .willReturn(true);
        given(adminRepository.save(any(Admin.class))).willReturn(mockAdmin);

        // when
        Optional<AdminResponse> result = adminService.login(validLoginRequest);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getLoginId()).isEqualTo("admin");
        assertThat(result.get().getName()).isEqualTo("관리자");

        // verify
        then(adminRepository).should().findByLoginId(validLoginRequest.getLoginId());
        then(passwordEncoder).should().matches(validLoginRequest.getPassword(), mockAdmin.getPassword());
        then(adminRepository).should().save(mockAdmin);
    }

    @Test
    @DisplayName("🔴 RED: 관리자 로그인 실패 테스트 - 존재하지 않는 관리자")
    void login_Failure_AdminNotFound() {
        // given
        given(adminRepository.findByLoginId(validLoginRequest.getLoginId()))
                .willReturn(Optional.empty());

        // when
        Optional<AdminResponse> result = adminService.login(validLoginRequest);

        // then
        assertThat(result).isEmpty();

        // verify
        then(adminRepository).should().findByLoginId(validLoginRequest.getLoginId());
        then(passwordEncoder).should(never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("🔴 RED: 관리자 로그인 실패 테스트 - 비밀번호 불일치")
    void login_Failure_PasswordMismatch() {
        // given
        given(adminRepository.findByLoginId(validLoginRequest.getLoginId()))
                .willReturn(Optional.of(mockAdmin));
        given(passwordEncoder.matches(validLoginRequest.getPassword(), mockAdmin.getPassword()))
                .willReturn(false);

        // when
        Optional<AdminResponse> result = adminService.login(validLoginRequest);

        // then
        assertThat(result).isEmpty();

        // verify
        then(adminRepository).should().findByLoginId(validLoginRequest.getLoginId());
        then(passwordEncoder).should().matches(validLoginRequest.getPassword(), mockAdmin.getPassword());
        then(adminRepository).should(never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("🔴 RED: 관리자 정보 조회 성공 테스트")
    void findById_Success() {
        // given
        Long adminId = 1L;
        given(adminRepository.findById(adminId)).willReturn(Optional.of(mockAdmin));

        // when
        Optional<AdminResponse> result = adminService.findById(adminId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getLoginId()).isEqualTo("admin");

        // verify
        then(adminRepository).should().findById(adminId);
    }

    @Test
    @DisplayName("🔴 RED: 관리자 정보 조회 실패 테스트 - 존재하지 않는 관리자")
    void findById_NotFound() {
        // given
        Long adminId = 999L;
        given(adminRepository.findById(adminId)).willReturn(Optional.empty());

        // when
        Optional<AdminResponse> result = adminService.findById(adminId);

        // then
        assertThat(result).isEmpty();

        // verify
        then(adminRepository).should().findById(adminId);
    }

    @Test
    @DisplayName("🔴 RED: 파트너 승인 성공 테스트")
    void approvePartner_Success() {
        // given
        Long partnerId = 1L;
        Member mockMember = createMockMember();
        MemberBusinessProfile mockBusinessProfile = createMockBusinessProfile();
        
        given(memberRepository.findById(partnerId)).willReturn(Optional.of(mockMember));
        given(memberBusinessProfileRepository.findByMemberId(partnerId))
                .willReturn(Optional.of(mockBusinessProfile));

        // when
        MemberResponse result = adminService.approvePartner(partnerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo("testpartner");

        // verify
        then(memberRepository).should().findById(partnerId);
        then(memberBusinessProfileRepository).should().findByMemberId(partnerId);
        verify(mockBusinessProfile).approve("관리자");
        assertThat(mockMember.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("🔴 RED: 파트너 승인 실패 테스트 - 존재하지 않는 회원")
    void approvePartner_Failure_MemberNotFound() {
        // given
        Long partnerId = 999L;
        given(memberRepository.findById(partnerId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.approvePartner(partnerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원을 찾을 수 없습니다.");

        // verify
        then(memberRepository).should().findById(partnerId);
        then(memberBusinessProfileRepository).should(never()).findByMemberId(anyLong());
    }

    @Test
    @DisplayName("🔴 RED: 파트너 승인 실패 테스트 - 사업자 프로필 없음")
    void approvePartner_Failure_BusinessProfileNotFound() {
        // given
        Long partnerId = 1L;
        Member mockMember = createMockMember();
        
        given(memberRepository.findById(partnerId)).willReturn(Optional.of(mockMember));
        given(memberBusinessProfileRepository.findByMemberId(partnerId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.approvePartner(partnerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파트너 상세 정보를 찾을 수 없습니다.");

        // verify
        then(memberRepository).should().findById(partnerId);
        then(memberBusinessProfileRepository).should().findByMemberId(partnerId);
    }

    @Test
    @DisplayName("🔴 RED: 파트너 거부 성공 테스트")
    void rejectPartner_Success() {
        // given
        Long partnerId = 1L;
        String reason = "서류 미비";
        Member mockMember = createMockMember();
        MemberBusinessProfile mockBusinessProfile = createMockBusinessProfile();
        
        given(memberRepository.findById(partnerId)).willReturn(Optional.of(mockMember));
        given(memberBusinessProfileRepository.findByMemberId(partnerId))
                .willReturn(Optional.of(mockBusinessProfile));

        // when
        MemberResponse result = adminService.rejectPartner(partnerId, reason);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo("testpartner");

        // verify
        then(memberRepository).should().findById(partnerId);
        then(memberBusinessProfileRepository).should().findByMemberId(partnerId);
        verify(mockBusinessProfile).reject(reason);
    }

    @Test
    @DisplayName("🔴 RED: 초기 관리자 생성 성공 테스트")
    void createInitialAdmin_Success() {
        // given
        String loginId = "admin";
        String password = "password123";
        String name = "관리자";
        
        given(adminRepository.existsByLoginId(loginId)).willReturn(false);
        given(adminRepository.save(any(Admin.class))).willReturn(mockAdmin);

        // when
        AdminResponse result = adminService.createInitialAdmin(loginId, password, name);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo("admin");
        assertThat(result.getName()).isEqualTo("관리자");

        // verify
        then(adminRepository).should().existsByLoginId(loginId);
        then(adminRepository).should().save(any(Admin.class));
    }

    @Test
    @DisplayName("🔴 RED: 초기 관리자 생성 실패 테스트 - 중복 로그인 ID")
    void createInitialAdmin_Failure_DuplicateLoginId() {
        // given
        String loginId = "admin";
        String password = "password123";
        String name = "관리자";
        
        given(adminRepository.existsByLoginId(loginId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminService.createInitialAdmin(loginId, password, name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 관리자 ID입니다.");

        // verify
        then(adminRepository).should().existsByLoginId(loginId);
        then(adminRepository).should(never()).save(any(Admin.class));
    }

    // === 테스트 데이터 생성 헬퍼 메서드들 ===

    private Admin createMockAdmin() {
        return Admin.builder()
                .id(1L)
                .loginId("admin")
                .password("$2a$10$encodedPassword")
                .name("관리자")
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    private AdminLoginRequest createValidLoginRequest() {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setLoginId("admin");
        request.setPassword("password123");
        return request;
    }

    private Member createMockMember() {
        Member member = new Member();
        member.setId(1L);
        member.setLoginId("testpartner");
        member.setName("테스트파트너");
        member.setStatus(Member.MemberStatus.PENDING);
        return member;
    }

    private MemberBusinessProfile createMockBusinessProfile() {
        return mock(MemberBusinessProfile.class);
    }
} 
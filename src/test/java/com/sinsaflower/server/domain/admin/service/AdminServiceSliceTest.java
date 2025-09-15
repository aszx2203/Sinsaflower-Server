package com.sinsaflower.server.domain.admin.service;

import com.sinsaflower.server.domain.admin.repository.AdminRepository;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.entity.MemberBusinessProfile;
import com.sinsaflower.server.domain.member.repository.MemberBusinessProfileRepository;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService 슬라이스 테스트")
class AdminServiceSliceTest {

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

    @Test
    @DisplayName("멤버 승인 성공")
    void approveMember_Success() {
        // given
        Long memberId = 1L;
        Member member = createPendingMember();
        MemberBusinessProfile businessProfile = createPendingBusinessProfile();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberBusinessProfileRepository.findByMemberId(memberId)).willReturn(Optional.of(businessProfile));

        // when
        MemberResponse result = adminService.approveMember(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo("testuser");
        assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
        assertThat(businessProfile.getApprovalStatus()).isEqualTo(MemberBusinessProfile.ApprovalStatus.APPROVED);

        verify(memberRepository).findById(memberId);
        verify(memberBusinessProfileRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("멤버 승인 실패 - 존재하지 않는 회원")
    void approveMember_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.approveMember(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        verify(memberRepository).findById(memberId);
        verify(memberBusinessProfileRepository, never()).findByMemberId(any());
    }

    @Test
    @DisplayName("멤버 승인 실패 - 사업자 프로필 없음")
    void approveMember_Fail_BusinessProfileNotFound() {
        // given
        Long memberId = 1L;
        Member member = createPendingMember();
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberBusinessProfileRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.approveMember(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("멤버 상세 정보를 찾을 수 없습니다");

        verify(memberRepository).findById(memberId);
        verify(memberBusinessProfileRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("멤버 거부 성공")
    void rejectMember_Success() {
        // given
        Long memberId = 1L;
        String rejectionReason = "서류 미비";
        Member member = createPendingMember();
        MemberBusinessProfile businessProfile = createPendingBusinessProfile();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberBusinessProfileRepository.findByMemberId(memberId)).willReturn(Optional.of(businessProfile));

        // when
        MemberResponse result = adminService.rejectMember(memberId, rejectionReason);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo("testuser");
        assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.PENDING); // 거부 후에도 PENDING 유지
        assertThat(businessProfile.getApprovalStatus()).isEqualTo(MemberBusinessProfile.ApprovalStatus.REJECTED);
        assertThat(businessProfile.getRejectionReason()).isEqualTo(rejectionReason);

        verify(memberRepository).findById(memberId);
        verify(memberBusinessProfileRepository).findByMemberId(memberId);
    }

    @Test
    @DisplayName("멤버 거부 실패 - 존재하지 않는 회원")
    void rejectMember_Fail_MemberNotFound() {
        // given
        Long memberId = 999L;
        String rejectionReason = "서류 미비";
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.rejectMember(memberId, rejectionReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        verify(memberRepository).findById(memberId);
        verify(memberBusinessProfileRepository, never()).findByMemberId(any());
    }

    // === 테스트 데이터 생성 헬퍼 메서드 ===

    private Member createPendingMember() {
        return Member.builder()
                .id(1L)
                .loginId("testuser")
                .password("encodedPassword")
                .name("테스트화환")
                .nickname("테스트")
                .mobile("010-1234-5678")
                .status(Member.MemberStatus.PENDING)
                .build();
    }

    private MemberBusinessProfile createPendingBusinessProfile() {
        return MemberBusinessProfile.builder()
                .id(1L)
                .businessNumber("123-45-67890")
                .corpName("테스트 주식회사")
                .ceoName("김테스트")
                .businessType("농업")
                .businessItem("화훼재배업")
                .companyAddress("서울 강남구 테스트로 123")
                .approvalStatus(MemberBusinessProfile.ApprovalStatus.PENDING)
                .build();
    }
}

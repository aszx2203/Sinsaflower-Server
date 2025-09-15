package com.sinsaflower.server.domain.member.service;

import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.entity.MemberBusinessProfile;
import com.sinsaflower.server.domain.member.repository.*;
import com.sinsaflower.server.domain.product.repository.MemberProductPriceRepository;
import com.sinsaflower.server.global.service.FileUploadService;
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
@DisplayName("MemberService 슬라이스 테스트")
class MemberServiceSliceTest {

    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private MemberBusinessProfileRepository businessProfileRepository;
    
    @Mock
    private MemberBankAccountRepository bankAccountRepository;
    
    @Mock
    private MemberProductPriceRepository productPriceRepository;
    
    @Mock
    private MemberActivityRegionRepository activityRegionRepository;
    
    @Mock
    private NotificationSettingRepository notificationSettingRepository;
    
    @Mock
    private FileUploadService fileUploadService;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입 성공 - 기본 정보만으로")
    void signUp_Success_BasicInfo() {
        // given
        MemberSignupRequest request = createBasicSignupRequest();
        Member savedMember = createMemberEntity();
        MemberBusinessProfile savedProfile = createBusinessProfileEntity();

        given(memberRepository.existsByLoginId(anyString())).willReturn(false);
        given(businessProfileRepository.existsByBusinessNumber(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(businessProfileRepository.save(any(MemberBusinessProfile.class))).willReturn(savedProfile);

        // when
        MemberResponse result = memberService.signUp(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo("testuser");
        assertThat(result.getName()).isEqualTo("테스트화환");
        assertThat(result.getStatus()).isEqualTo("승인대기");

        verify(memberRepository).existsByLoginId("testuser");
        verify(businessProfileRepository).existsByBusinessNumber("123-45-67890");
        verify(memberRepository).save(any(Member.class));
        verify(businessProfileRepository).save(any(MemberBusinessProfile.class));
    }

    @Test
    @DisplayName("회원 가입 실패 - 중복 로그인 ID")
    void signUp_Fail_DuplicateLoginId() {
        // given
        MemberSignupRequest request = createBasicSignupRequest();
        given(memberRepository.existsByLoginId(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 로그인 ID입니다");

        verify(memberRepository).existsByLoginId("testuser");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 가입 실패 - 중복 사업자등록번호")
    void signUp_Fail_DuplicateBusinessNumber() {
        // given
        MemberSignupRequest request = createBasicSignupRequest();
        given(memberRepository.existsByLoginId(anyString())).willReturn(false);
        given(businessProfileRepository.existsByBusinessNumber(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 등록된 사업자등록번호입니다");

        verify(businessProfileRepository).existsByBusinessNumber("123-45-67890");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("로그인 ID 중복 확인 - 존재함")
    void isLoginIdDuplicate_Exists() {
        // given
        given(memberRepository.existsByLoginId("testuser")).willReturn(true);

        // when
        boolean result = memberService.isLoginIdDuplicate("testuser");

        // then
        assertThat(result).isTrue();
        verify(memberRepository).existsByLoginId("testuser");
    }

    @Test
    @DisplayName("로그인 ID 중복 확인 - 존재하지 않음")
    void isLoginIdDuplicate_NotExists() {
        // given
        given(memberRepository.existsByLoginId("newuser")).willReturn(false);

        // when
        boolean result = memberService.isLoginIdDuplicate("newuser");

        // then
        assertThat(result).isFalse();
        verify(memberRepository).existsByLoginId("newuser");
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberInfo_Success() {
        // given
        Long memberId = 1L;
        Member member = createMemberEntity();
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        MemberResponse result = memberService.getMemberInfo(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);
        assertThat(result.getLoginId()).isEqualTo("testuser");
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void getMemberInfo_Fail_NotFound() {
        // given
        Long memberId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberInfo(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        verify(memberRepository).findById(memberId);
    }

    // === 테스트 데이터 생성 헬퍼 메서드 ===

    private MemberSignupRequest createBasicSignupRequest() {
        MemberSignupRequest request = new MemberSignupRequest();
        request.setLoginId("testuser");
        request.setPassword("password123");
        request.setName("테스트화환");
        request.setNickname("테스트");
        request.setMobile("010-1234-5678");

        // 사업자 프로필 (필수 정보만)
        MemberSignupRequest.BusinessProfileRequest businessProfile = new MemberSignupRequest.BusinessProfileRequest();
        businessProfile.setBusinessNumber("123-45-67890");
        businessProfile.setCorpName("테스트 주식회사");
        businessProfile.setCeoName("김테스트");
        businessProfile.setBusinessType("농업");
        businessProfile.setBusinessItem("화훼재배업");
        businessProfile.setCompanyAddress("서울 강남구 테스트로 123");

        // 간단한 주소 정보
        MemberSignupRequest.AddressRequest officeAddress = new MemberSignupRequest.AddressRequest();
        officeAddress.setSido("서울");
        officeAddress.setSigungu("강남구");
        officeAddress.setDetail("테스트로 123");
        officeAddress.setZipcode("12345");

        businessProfile.setOfficeAddress(officeAddress);
        request.setBusinessProfile(businessProfile);

        return request;
    }

    private Member createMemberEntity() {
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

    private MemberBusinessProfile createBusinessProfileEntity() {
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

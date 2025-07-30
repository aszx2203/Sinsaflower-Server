package com.sinsaflower.server.integration;

import com.sinsaflower.server.domain.admin.service.AdminService;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import com.sinsaflower.server.domain.member.service.MemberService;
import com.sinsaflower.server.domain.common.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
@DisplayName("회원 승인 통합 테스트")
class MemberApprovalIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("통합 테스트: 회원 가입 → 관리자 승인 → 상태 변경 전체 플로우")
    void memberSignupAndApprovalFlow() {
        // given - 회원 가입 요청 데이터
        MemberSignupRequest signupRequest = createSignupRequest();

        // when - 1단계: 회원 가입
        MemberResponse memberResponse = memberService.signUp(signupRequest);

        // then - 회원 가입 검증
        assertThat(memberResponse).isNotNull();
        assertThat(memberResponse.getLoginId()).isEqualTo("integrationtest");
        assertThat(memberResponse.getStatus()).isEqualTo("승인대기");

        // given - 2단계: 관리자 승인
        Long memberId = memberResponse.getId();

        // when - 관리자가 파트너 승인
        MemberResponse approvedMember = adminService.approvePartner(memberId);

        // then - 승인 후 상태 검증
        assertThat(approvedMember).isNotNull();
        assertThat(approvedMember.getLoginId()).isEqualTo("integrationtest");

        // DB에서 실제 상태 확인
        Member memberFromDB = memberRepository.findById(memberId).orElse(null);
        assertThat(memberFromDB).isNotNull();
        assertThat(memberFromDB.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("통합 테스트: 회원 가입 → 관리자 거부 → 상태 변경 전체 플로우")
    void memberSignupAndRejectionFlow() {
        // given - 회원 가입 요청 데이터
        MemberSignupRequest signupRequest = createSignupRequest();

        // when - 1단계: 회원 가입
        MemberResponse memberResponse = memberService.signUp(signupRequest);

        // then - 회원 가입 검증
        assertThat(memberResponse).isNotNull();
        assertThat(memberResponse.getLoginId()).isEqualTo("integrationtest");
        assertThat(memberResponse.getStatus()).isEqualTo("승인대기");

        // given - 2단계: 관리자 거부
        Long memberId = memberResponse.getId();
        String rejectionReason = "서류 미비";

        // when - 관리자가 파트너 거부
        MemberResponse rejectedMember = adminService.rejectPartner(memberId, rejectionReason);

        // then - 거부 후 상태 검증
        assertThat(rejectedMember).isNotNull();
        assertThat(rejectedMember.getLoginId()).isEqualTo("integrationtest");

        // DB에서 실제 상태 확인 (거부 후에도 PENDING 상태 유지)
        Member memberFromDB = memberRepository.findById(memberId).orElse(null);
        assertThat(memberFromDB).isNotNull();
        assertThat(memberFromDB.getStatus()).isEqualTo(Member.MemberStatus.PENDING);
    }

    @Test
    @DisplayName("통합 테스트: 중복 로그인 ID 검증")
    void duplicateLoginIdValidation() {
        // given - 첫 번째 회원 가입
        MemberSignupRequest firstRequest = createSignupRequest();
        memberService.signUp(firstRequest);

        // when & then - 동일한 로그인 ID로 재가입 시도
        MemberSignupRequest duplicateRequest = createSignupRequest();
        
        assertThatThrownBy(() -> memberService.signUp(duplicateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 로그인 ID입니다");
    }

    @Test
    @DisplayName("통합 테스트: 중복 확인 API 동작")
    void duplicateCheckIntegration() {
        // given - 회원 가입
        MemberSignupRequest signupRequest = createSignupRequest();
        memberService.signUp(signupRequest);

        // when & then - 로그인 ID 중복 확인
        boolean isLoginIdDuplicate = memberService.isLoginIdDuplicate("integrationtest");
        assertThat(isLoginIdDuplicate).isTrue();

        // when & then - 사업자등록번호 중복 확인
        boolean isBusinessNumberDuplicate = memberService.isBusinessNumberDuplicate("123-45-67890");
        assertThat(isBusinessNumberDuplicate).isTrue();

        // when & then - 존재하지 않는 데이터 확인
        boolean isNewIdDuplicate = memberService.isLoginIdDuplicate("newuser");
        assertThat(isNewIdDuplicate).isFalse();
    }

    // === 테스트 데이터 생성 헬퍼 메서드 ===

    private MemberSignupRequest createSignupRequest() {
        MemberSignupRequest request = new MemberSignupRequest();
        request.setLoginId("integrationtest");
        request.setPassword("password123");
        request.setName("통합테스트화환");
        request.setNickname("통합테스트");
        request.setMobile("010-9999-9999");

        // 사업자 프로필 정보
        MemberSignupRequest.BusinessProfileRequest businessProfile = new MemberSignupRequest.BusinessProfileRequest();
        businessProfile.setBusinessNumber("123-45-67890");
        businessProfile.setCorpName("통합테스트 주식회사");
        businessProfile.setCeoName("홍길동");
        businessProfile.setBusinessType("농업");
        businessProfile.setBusinessItem("화훼재배업");
        businessProfile.setBankName("국민은행");
        businessProfile.setAccountNumber("123456-78-901234");
        businessProfile.setAccountOwner("홍길동");
        businessProfile.setCompanyAddress("서울 강남구 테스트로 456 12345");

        MemberSignupRequest.AddressRequest officeAddress = new MemberSignupRequest.AddressRequest();
        officeAddress.setSido("서울");
        officeAddress.setSigungu("강남구");
        officeAddress.setDetail("테스트로 456");
        officeAddress.setZipcode("12345");

        request.setBusinessProfile(businessProfile);

        return request;
    }
} 
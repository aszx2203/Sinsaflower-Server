package com.sinsaflower.server.domain.member.controller;

// import com.sinsaflower.server.domain.member.controller.MemberValidationController.DuplicateCheckResponse;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.domain.member.service.MemberService;
import com.sinsaflower.server.global.controller.AuthController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController 단위 테스트")
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;
    private AuthController authController;
    private MemberValidationController memberValidationController;

    private MemberSignupRequest validSignupRequest;
    private MemberResponse mockMemberResponse;

    @BeforeEach
    void setUp() {
        validSignupRequest = createValidSignupRequest();
        mockMemberResponse = createMockMemberResponse();
    }

    // @Test
    // @DisplayName("회원 가입 API 성공 테스트")
    // void signup_Success() {
    //     // given
    //     given(memberService.signUp(any(MemberSignupRequest.class))).willReturn(mockMemberResponse);

    //     // when
    //     ResponseEntity<MemberResponse> response = authController.signup(validSignupRequest);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    //     assertThat(response.getBody()).isNotNull();
    //     assertThat(response.getBody().getLoginId()).isEqualTo("testuser");
    //     assertThat(response.getBody().getName()).isEqualTo("테스트화환");
    //     assertThat(response.getBody().getStatus()).isEqualTo("승인대기");

    //     // verify
    //     then(memberService).should().signUp(any(MemberSignupRequest.class));
    // }

    // @Test
    // @DisplayName("회원 가입 API 실패 테스트 - 중복 데이터")
    // void signup_Failure_DuplicateData() {
    //     // given
    //     given(memberService.signUp(any(MemberSignupRequest.class)))
    //             .willThrow(new IllegalArgumentException("이미 사용 중인 로그인 ID입니다"));

    //     // when
    //     ResponseEntity<MemberResponse> response = authController.signup(validSignupRequest);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    //     assertThat(response.getBody()).isNull();

    //     // verify
    //     then(memberService).should().signUp(any(MemberSignupRequest.class));
    // }

    // @Test
    // @DisplayName("회원 가입 API 실패 테스트 - 서버 오류")
    // void signup_Failure_ServerError() {
    //     // given
    //     given(memberService.signUp(any(MemberSignupRequest.class)))
    //             .willThrow(new RuntimeException("서버 내부 오류"));

    //     // when
    //     // ResponseEntity<MemberResponse> response = authController.signup(validSignupRequest);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    //     assertThat(response.getBody()).isNull();

    //     // verify
    //     then(memberService).should().signUp(any(MemberSignupRequest.class));
    // }

    // @Test
    // @DisplayName("회원 정보 조회 API 성공 테스트")
    // void getMemberInfo_Success() {
    //     // given
    //     Long memberId = 1L;
    //     given(memberService.getMemberInfo(memberId)).willReturn(mockMemberResponse);

    //     // when
    //     ResponseEntity<MemberResponse> response = memberController.getMemberInfo(memberId);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(response.getBody()).isNotNull();
    //     assertThat(response.getBody().getId()).isEqualTo(1L);
    //     assertThat(response.getBody().getLoginId()).isEqualTo("testuser");

    //     // verify
    //     then(memberService).should().getMemberInfo(memberId);
    // }

    // @Test
    // @DisplayName("회원 정보 조회 API 실패 테스트 - 존재하지 않는 회원")
    // void getMemberInfo_NotFound() {
    //     // given
    //     Long memberId = 999L;
    //     given(memberService.getMemberInfo(memberId))
    //             .willThrow(new IllegalArgumentException("회원을 찾을 수 없습니다"));

    //     // when
    //     ResponseEntity<MemberResponse> response = memberController.getMemberInfo(memberId);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    //     assertThat(response.getBody()).isNull();

    //     // verify
    //     then(memberService).should().getMemberInfo(memberId);
    // }

    // @Test
    // @DisplayName("로그인 ID로 회원 조회 API 성공 테스트")
    // void getMemberByLoginId_Success() {
    //     // given
    //     String loginId = "testuser";
    //     given(memberService.getMemberByLoginId(loginId)).willReturn(mockMemberResponse);

    //     // when
    //     ResponseEntity<MemberResponse> response = memberController.getMemberByLoginId(loginId);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(response.getBody()).isNotNull();
    //     assertThat(response.getBody().getLoginId()).isEqualTo("testuser");

    //     // verify
    //     then(memberService).should().getMemberByLoginId(loginId);
    // }

    // @Test
    // @DisplayName("로그인 ID 중복 확인 API 테스트 - 중복된 ID")
    // void checkLoginIdDuplicate_Duplicate() {
    //     // given
    //     String loginId = "testuser";
    //     given(memberService.isLoginIdDuplicate(loginId)).willReturn(true);

    //     // when
    //     ResponseEntity<DuplicateCheckResponse> response = memberValidationController.checkLoginIdDuplicate(loginId);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(response.getBody()).isNotNull();
    //     assertThat(response.getBody().get("loginId")).isEqualTo("testuser");

    //     // verify
    //     then(memberService).should().isLoginIdDuplicate(loginId);
    // }

    // @Test
    // @DisplayName("로그인 ID 중복 확인 API 테스트 - 사용 가능한 ID")
    // void checkLoginIdDuplicate_Available() {
    //     // given
    //     String loginId = "available_user";
    //     given(memberService.isLoginIdDuplicate(loginId)).willReturn(false);

    //     // when
    //     ResponseEntity<Map<String, Object>> response = memberController.checkLoginIdDuplicate(loginId);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(response.getBody()).isNotNull();
    //     assertThat(response.getBody().get("loginId")).isEqualTo("available_user");
    //     assertThat(response.getBody().get("isDuplicate")).isEqualTo(false);
    //     assertThat(response.getBody().get("message")).isEqualTo("사용 가능한 ID입니다.");

    //     // verify
    //     then(memberService).should().isLoginIdDuplicate(loginId);
    // }

    // @Test
    // @DisplayName("사업자등록번호 중복 확인 API 테스트 - 중복된 번호")
    // void checkBusinessNumberDuplicate_Duplicate() {
    //     // given
    //     String businessNumber = "123-45-67890";
    //     given(memberService.isBusinessNumberDuplicate(businessNumber)).willReturn(true);

    //     // when
    //     ResponseEntity<Map<String, Object>> response = memberController.checkBusinessNumberDuplicate(businessNumber);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(response.getBody()).isNotNull();
    //     assertThat(response.getBody().get("businessNumber")).isEqualTo("123-45-67890");
    //     assertThat(response.getBody().get("isDuplicate")).isEqualTo(true);
    //     assertThat(response.getBody().get("message")).isEqualTo("이미 등록된 사업자등록번호입니다.");

    //     // verify
    //     then(memberService).should().isBusinessNumberDuplicate(businessNumber);
    // }

    // @Test
    // @DisplayName("사업자등록번호 중복 확인 API 테스트 - 사용 가능한 번호")
    // void checkBusinessNumberDuplicate_Available() {
    //     // given
    //     String businessNumber = "987-65-43210";
    //     given(memberService.isBusinessNumberDuplicate(businessNumber)).willReturn(false);

    //     // when
    //     ResponseEntity<Map<String, Object>> response = memberController.checkBusinessNumberDuplicate(businessNumber);

    //     // then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(response.getBody()).isNotNull();
    //     assertThat(response.getBody().get("businessNumber")).isEqualTo("987-65-43210");
    //     assertThat(response.getBody().get("isDuplicate")).isEqualTo(false);
    //     assertThat(response.getBody().get("message")).isEqualTo("등록 가능한 사업자등록번호입니다.");

    //     // verify
    //     then(memberService).should().isBusinessNumberDuplicate(businessNumber);
    // }

    // === 테스트 데이터 생성 헬퍼 메서드들 ===

    private MemberSignupRequest createValidSignupRequest() {
        MemberSignupRequest request = new MemberSignupRequest();
        request.setLoginId("testuser");
        request.setPassword("password123");
        request.setName("테스트화환");
        request.setNickname("테스트닉네임");
        request.setMobile("010-1234-5678");
        return request;
    }

    private MemberResponse createMockMemberResponse() {
        return MemberResponse.builder()
                .id(1L)
                .loginId("testuser")
                .name("테스트화환")
                .nickname("테스트닉네임")
                .mobile("010-1234-5678")
                .status("승인대기")
                .createdAt(LocalDateTime.now())
                .build();
    }
} 
package com.sinsaflower.server.domain.member.controller;

import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.service.MemberService;
import com.sinsaflower.server.global.dto.ApiResponse;
import com.sinsaflower.server.global.security.CustomUserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController 단위 테스트")
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MemberResponse mockMemberResponse;
    private CustomUserDetails mockMemberUser;

    @BeforeEach
    void setUp() {
        mockMemberResponse = createMockMemberResponse();
        mockMemberUser = createMockMemberUser();
    }

    @Test
    @DisplayName("내 정보 조회 API 성공 테스트")
    void getMyInfo_Success() {
        // given
        given(memberService.getMemberInfo(1L)).willReturn(mockMemberResponse);

        // when
        ResponseEntity<ApiResponse<MemberResponse>> response = memberController.getMyInfo(mockMemberUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData().getLoginId()).isEqualTo("testuser");
        assertThat(response.getBody().getData().getName()).isEqualTo("테스트화환");
        assertThat(response.getBody().getData().getStatus()).isEqualTo("승인대기");
        assertThat(response.getBody().getMessage()).isEqualTo("회원 정보 조회가 성공적으로 완료되었습니다.");

        // verify
        then(memberService).should().getMemberInfo(1L);
    }

    @Test
    @DisplayName("내 정보 조회 API 성공 테스트 - 다른 사용자")
    void getMyInfo_Success_DifferentUser() {
        // given
        CustomUserDetails anotherUser = new CustomUserDetails(
                2L,
                "anotheruser",
                CustomUserDetails.USER_TYPE_PARTNER,
                List.of(new SimpleGrantedAuthority(CustomUserDetails.ROLE_PARTNER))
        );
        
        MemberResponse anotherMemberResponse = MemberResponse.builder()
                .id(2L)
                .loginId("anotheruser")
                .name("다른사용자")
                .nickname("다른닉네임")
                .mobile("010-9876-5432")
                .status("활성")
                .createdAt(LocalDateTime.now())
                .build();
        
        given(memberService.getMemberInfo(2L)).willReturn(anotherMemberResponse);

        // when
        ResponseEntity<ApiResponse<MemberResponse>> response = memberController.getMyInfo(anotherUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData().getLoginId()).isEqualTo("anotheruser");
        assertThat(response.getBody().getData().getName()).isEqualTo("다른사용자");
        assertThat(response.getBody().getData().getStatus()).isEqualTo("활성");

        // verify
        then(memberService).should().getMemberInfo(2L);
    }

    @Test
    @DisplayName("내 정보 조회 API 테스트 - 관리자 사용자")
    void getMyInfo_Success_AdminUser() {
        // given
        CustomUserDetails adminUser = new CustomUserDetails(
                3L,
                "admin",
                CustomUserDetails.USER_TYPE_ADMIN,
                List.of(new SimpleGrantedAuthority(CustomUserDetails.ROLE_ADMIN))
        );
        
        MemberResponse adminResponse = MemberResponse.builder()
                .id(3L)
                .loginId("admin")
                .name("관리자")
                .nickname("관리자닉네임")
                .mobile("010-0000-0000")
                .status("관리자")
                .createdAt(LocalDateTime.now())
                .build();
        
        given(memberService.getMemberInfo(3L)).willReturn(adminResponse);

        // when
        ResponseEntity<ApiResponse<MemberResponse>> response = memberController.getMyInfo(adminUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData().getLoginId()).isEqualTo("admin");
        assertThat(response.getBody().getData().getName()).isEqualTo("관리자");

        // verify
        then(memberService).should().getMemberInfo(3L);
    }

    // === 테스트 데이터 생성 헬퍼 메서드들 ===

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

    private CustomUserDetails createMockMemberUser() {
        return new CustomUserDetails(
                1L, 
                "testuser", 
                CustomUserDetails.USER_TYPE_PARTNER, 
                List.of(new SimpleGrantedAuthority(CustomUserDetails.ROLE_PARTNER))
        );
    }
} 
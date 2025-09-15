package com.sinsaflower.server.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinsaflower.server.domain.admin.dto.AdminResponse;
import com.sinsaflower.server.domain.admin.service.AdminService;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController 단위 테스트")
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private AdminController adminController;

    private AdminResponse mockAdminResponse;
    private MemberResponse mockMemberResponse;
    private CustomUserDetails mockAdminUser;

    @BeforeEach
    void setUp() {
        mockAdminResponse = createMockAdminResponse();
        mockMemberResponse = createMockMemberResponse();
        mockAdminUser = createMockAdminUser();
    }

    @Test
    @DisplayName("관리자 내 정보 조회 API 성공 테스트")
    void getMyInfo_Success() {
        // given
        given(adminService.findById(1L)).willReturn(Optional.of(mockAdminResponse));

        // when
        ResponseEntity<ApiResponse<AdminResponse>> response = adminController.getMyInfo(mockAdminUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData().getLoginId()).isEqualTo("admin");
        assertThat(response.getBody().getData().getName()).isEqualTo("관리자");

        // verify
        then(adminService).should().findById(1L);
    }

    @Test
    @DisplayName("관리자 내 정보 조회 API 실패 테스트 - 관리자 없음")
    void getMyInfo_Failure_AdminNotFound() {
        // given
        given(adminService.findById(1L)).willReturn(Optional.empty());

        // when
        ResponseEntity<ApiResponse<AdminResponse>> response = adminController.getMyInfo(mockAdminUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getCode()).isEqualTo(404);

        // verify
        then(adminService).should().findById(1L);
    }

    @Test
    @DisplayName("승인 대기 멤버 목록 조회 API 성공 테스트")
    void getPendingMembers_Success() {
        // given
        List<MemberResponse> pendingMembers = List.of(mockMemberResponse);
        given(memberService.getPendingMembers()).willReturn(pendingMembers);

        // when
        ResponseEntity<ApiResponse<List<MemberResponse>>> response = adminController.getPendingMembers(mockAdminUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getLoginId()).isEqualTo("testuser");

        // verify
        then(memberService).should().getPendingMembers();
    }

    @Test
    @DisplayName("멤버 승인 API 성공 테스트")
    void approveMember_Success() {
        // given
        Long memberId = 1L;
        given(adminService.approveMember(memberId)).willReturn(mockMemberResponse);

        // when
        ResponseEntity<ApiResponse<MemberResponse>> response = adminController.approveMember(memberId, mockAdminUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData().getLoginId()).isEqualTo("testuser");
        assertThat(response.getBody().getMessage()).isEqualTo("멤버 승인이 완료되었습니다.");

        // verify
        then(adminService).should().approveMember(memberId);
    }

    @Test
    @DisplayName("멤버 승인 거부 API 성공 테스트")
    void rejectMember_Success() {
        // given
        Long memberId = 1L;
        String rejectionReason = "서류 불비";
        Map<String, String> request = Map.of("reason", rejectionReason);
        given(adminService.rejectMember(eq(memberId), anyString())).willReturn(mockMemberResponse);

        // when
        ResponseEntity<ApiResponse<MemberResponse>> response = adminController.rejectMember(memberId, request, mockAdminUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData().getLoginId()).isEqualTo("testuser");
        assertThat(response.getBody().getMessage()).isEqualTo("멤버 승인 거부가 완료되었습니다.");

        // verify
        then(adminService).should().rejectMember(eq(memberId), eq(rejectionReason));
    }

    @Test
    @DisplayName("멤버 승인 거부 API 실패 테스트 - 거부 사유 누락")
    void rejectMember_Failure_MissingReason() {
        // given
        Long memberId = 1L;
        Map<String, String> request = Map.of(); // 빈 맵

        // when
        ResponseEntity<ApiResponse<MemberResponse>> response = adminController.rejectMember(memberId, request, mockAdminUser);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("거부 사유는 필수입니다.");

        // verify - 서비스 호출되지 않음
        then(adminService).should(never()).rejectMember(anyLong(), anyString());
    }

    @Test
    @DisplayName("초기 관리자 생성 API 성공 테스트")
    void createInitialAdmin_Success() {
        // given
        AdminResponse newAdminResponse = AdminResponse.builder()
                .id(2L)
                .loginId("newadmin")
                .name("새 관리자")
                .createdAt(LocalDateTime.now())
                .build();
        
        Map<String, String> request = Map.of(
            "loginId", "newadmin",
            "password", "password123",
            "name", "새 관리자"
        );
        
        given(adminService.createInitialAdmin("newadmin", "password123", "새 관리자"))
                .willReturn(newAdminResponse);

        // when
        ResponseEntity<ApiResponse<AdminResponse>> response = adminController.createInitialAdmin(request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().getCode()).isEqualTo(201);
        assertThat(response.getBody().getData().getLoginId()).isEqualTo("newadmin");
        assertThat(response.getBody().getData().getName()).isEqualTo("새 관리자");

        // verify
        then(adminService).should().createInitialAdmin("newadmin", "password123", "새 관리자");
    }

    @Test
    @DisplayName("초기 관리자 생성 API 실패 테스트 - 필수 정보 누락")
    void createInitialAdmin_Failure_MissingInfo() {
        // given
        Map<String, String> request = Map.of("loginId", "newadmin"); // password, name 누락

        // when
        ResponseEntity<ApiResponse<AdminResponse>> response = adminController.createInitialAdmin(request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("loginId, password, name은 필수 정보입니다.");

        // verify - 서비스 호출되지 않음
        then(adminService).should(never()).createInitialAdmin(anyString(), anyString(), anyString());
    }

    // === 테스트 데이터 생성 헬퍼 메서드들 ===

    private AdminResponse createMockAdminResponse() {
        return AdminResponse.builder()
                .id(1L)
                .loginId("admin")
                .name("관리자")
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
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

    private CustomUserDetails createMockAdminUser() {
        return new CustomUserDetails(
                1L, 
                "admin", 
                CustomUserDetails.USER_TYPE_ADMIN, 
                List.of(new SimpleGrantedAuthority(CustomUserDetails.ROLE_ADMIN))
        );
    }
}
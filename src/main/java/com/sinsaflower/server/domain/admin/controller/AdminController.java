package com.sinsaflower.server.domain.admin.controller;

import com.sinsaflower.server.domain.admin.dto.AdminResponse;
import com.sinsaflower.server.domain.admin.service.AdminService;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.entity.Member.MemberStatus;
import com.sinsaflower.server.domain.member.service.MemberService;
import com.sinsaflower.server.global.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 관리자 전용 API 컨트롤러
 * 멤버 승인, 관리자 관리 등의 기능 제공
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 API", description = "관리자 전용 기능 - 멤버 승인, 관리자 관리 등")
public class AdminController {

    private final AdminService adminService;
    private final MemberService memberService;

    /**
     * 내 관리자 정보 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 관리자 정보 조회", description = "현재 로그인한 관리자의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                     content = @Content(schema = @Schema(implementation = AdminResponse.class))),
        @ApiResponse(responseCode = "404", description = "관리자를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<AdminResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("관리자 정보 조회 요청: {} (ID: {})", currentUser.getUsername(), currentUser.getUserId());
        
        Optional<AdminResponse> response = adminService.findById(currentUser.getUserId());
        if (response.isPresent()) {
            return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("관리자 정보 조회가 완료되었습니다.", response.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(com.sinsaflower.server.global.dto.ApiResponse.notFound("관리자를 찾을 수 없습니다."));
        }
    }


    /**
     * 승인 대기 중인 멤버 목록 조회
     */
    @GetMapping("/members/pending")
    @Operation(summary = "승인 대기 멤버 목록 조회", description = "승인 대기 중인 멤버 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<List<MemberResponse>>> getPendingMembers(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("승인 대기 중인 멤버 목록 조회 요청 by {} (ID: {})", currentUser.getUsername(), currentUser.getUserId());
        
        List<MemberResponse> pendingMembers = memberService.getPendingMembers();
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("승인 대기 중인 멤버 목록 조회가 완료되었습니다.", pendingMembers));
    }

    /**
     * 멤버 승인 처리
     */
    @PostMapping("/members/{memberId}/approve")
    @Operation(summary = "멤버 승인", description = "대기 중인 멤버의 가입을 승인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "승인 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 처리된 멤버 등)"),
        @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> approveMember(
            @Parameter(description = "멤버 ID", required = true) @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("멤버 승인 요청 - 멤버: {} by {} (ID: {})", memberId, currentUser.getUsername(), currentUser.getUserId());
        
        MemberResponse response = adminService.approveMember(memberId);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("멤버 승인이 완료되었습니다.", response));
    }

    /**
     * 멤버 승인 거부
     */
    @PostMapping("/members/{memberId}/reject")
    @Operation(summary = "멤버 승인 거부", description = "대기 중인 멤버의 가입을 거부합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "거부 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (거부 사유 누락 등)"),
        @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> rejectMember(
            @Parameter(description = "멤버 ID", required = true) @PathVariable Long memberId,
            @RequestBody 
            @Schema(description = "승인 거부 사유", example = """
                {
                    "reason": "사업자등록증이 불분명하여 승인이 어렵습니다. 다시 제출해 주세요."
                }
                """) 
            Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("멤버 거부 요청 - 멤버: {} by {} (ID: {})", memberId, currentUser.getUsername(), currentUser.getUserId());
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(com.sinsaflower.server.global.dto.ApiResponse.badRequest("거부 사유는 필수입니다."));
        }
        
        MemberResponse response = adminService.rejectMember(memberId, reason);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("멤버 승인 거부가 완료되었습니다.", response));
    }

    /**
     * 초기 관리자 생성 (개발용)
     * 인증이 필요없는 엔드포인트 (최초 관리자 생성용)
     */
    @PostMapping("/init")
    @Operation(summary = "초기 관리자 생성 (개발용)", description = "시스템 초기 설정을 위한 관리자 계정을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "관리자 생성 성공",
                     content = @Content(schema = @Schema(implementation = AdminResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 정보 누락)"),
        @ApiResponse(responseCode = "409", description = "이미 관리자가 존재함")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<AdminResponse>> createInitialAdmin(
            @RequestBody 
            @Schema(description = "관리자 생성 정보", example = """
                {
                    "loginId": "admin",
                    "password": "adminPassword123",
                    "name": "시스템 관리자"
                }
                """) 
            Map<String, String> request) {
        log.info("초기 관리자 생성 요청");
        
        String loginId = request.get("loginId");
        String password = request.get("password");
        String name = request.get("name");
        
        if (loginId == null || password == null || name == null) {
            return ResponseEntity.badRequest()
                .body(com.sinsaflower.server.global.dto.ApiResponse.badRequest("loginId, password, name은 필수 정보입니다."));
        }
        
        AdminResponse response = adminService.createInitialAdmin(loginId, password, name);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(com.sinsaflower.server.global.dto.ApiResponse.created("초기 관리자가 성공적으로 생성되었습니다.", response));
    }

    /**
     * 모든 회원 조회 (페이징)
     */
    @GetMapping("/members/all")
    @Operation(summary = "모든 회원 조회", description = "관리자가 모든 회원을 조회합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Page<MemberResponse>>> getAllMembers(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("모든 회원 조회 요청 by admin: {}", currentUser.getUsername());
        
        Page<MemberResponse> response = memberService.getAllActiveMembers(pageable);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 목록 조회가 완료되었습니다.", response));
    }

    /**
     * 상태별 회원 조회
     */
    @GetMapping("/members/status/{status}")
    @Operation(summary = "상태별 회원 조회", description = "관리자가 특정 상태의 회원들을 조회합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Page<MemberResponse>>> getMembersByStatus(
            @PathVariable MemberStatus status,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("상태별 회원 조회 요청: {} by admin: {}", status, currentUser.getUsername());
        
        Page<MemberResponse> response = memberService.getMembersByStatus(status, pageable);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 목록 조회가 완료되었습니다.", response));
    }

    /**
     * 회원 검색
     */
    @GetMapping("/members/search")
    @Operation(summary = "회원 검색", description = "관리자가 이름으로 회원을 검색합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Page<MemberResponse>>> searchMembers(
            @RequestParam String name,
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("회원 검색 요청: {} by admin: {}", name, currentUser.getUsername());
        
        Page<MemberResponse> response = memberService.searchMembersByName(name, pageable);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 검색이 완료되었습니다.", response));
    }

    /**
     * 회원 정지
     */
    @PatchMapping("/members/{memberId}/suspend")
    @Operation(summary = "회원 정지", description = "관리자가 회원을 정지시킵니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> suspendMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("회원 정지 요청: {} by admin: {}", memberId, currentUser.getUsername());
        
        MemberResponse response = adminService.suspendMember(memberId);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 정지가 완료되었습니다.", response));
    }

    /**
     * 회원 정지 해제
     */
    @PatchMapping("/members/{memberId}/unsuspend")
    @Operation(summary = "회원 정지 해제", description = "관리자가 정지된 회원의 정지를 해제합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> unsuspendMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("회원 정지 해제 요청: {} by admin: {}", memberId, currentUser.getUsername());
        
        MemberResponse response = adminService.unsuspendMember(memberId);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 정지 해제가 완료되었습니다.", response));
    }

    /**
     * 회원 삭제 (소프트 삭제)
     */
    @DeleteMapping("/members/{memberId}")
    @Operation(summary = "회원 삭제", description = "관리자가 회원을 소프트 삭제합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Void>> deleteMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("회원 삭제 요청: {} by admin: {}", memberId, currentUser.getUsername());
        
        adminService.deleteMember(memberId, currentUser.getUsername());
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 삭제가 완료되었습니다."));
    }

    /**
     * 회원 통계 조회
     */
    @GetMapping("/members/statistics")
    @Operation(summary = "회원 통계 조회", description = "관리자가 회원 통계를 조회합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Map<String, Long>>> getMemberStatistics(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("회원 통계 조회 요청 by admin: {}", currentUser.getUsername());
        
        Map<String, Long> statistics = adminService.getMemberStatistics();
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 통계 조회가 완료되었습니다.", statistics));
    }
} 
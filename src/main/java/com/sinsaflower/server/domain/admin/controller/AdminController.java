package com.sinsaflower.server.domain.admin.controller;

import com.sinsaflower.server.domain.admin.dto.AdminLoginRequest;
import com.sinsaflower.server.domain.admin.dto.AdminResponse;
import com.sinsaflower.server.domain.admin.service.AdminService;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 관리자 전용 API 컨트롤러
 * 파트너 승인, 관리자 관리 등의 기능 제공
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 API", description = "관리자 전용 기능 - 파트너 승인, 관리자 관리 등")
public class AdminController {

    private final AdminService adminService;
    private final MemberService memberService;

    /**
     * 관리자 정보 조회
     */
    @GetMapping("/{adminId}")
    @Operation(summary = "관리자 정보 조회", description = "관리자 ID로 관리자의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                     content = @Content(schema = @Schema(implementation = AdminResponse.class))),
        @ApiResponse(responseCode = "404", description = "관리자를 찾을 수 없음")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<AdminResponse>> getAdmin(
            @Parameter(description = "관리자 ID", required = true) @PathVariable Long adminId) {
        log.info("관리자 정보 조회 요청: {}", adminId);
        
        Optional<AdminResponse> response = adminService.findById(adminId);
        if (response.isPresent()) {
            return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("관리자 정보 조회가 완료되었습니다.", response.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(com.sinsaflower.server.global.dto.ApiResponse.notFound("관리자를 찾을 수 없습니다. ID: " + adminId));
        }
    }

    /**
     * 승인 대기 중인 파트너 목록 조회
     */
    @GetMapping("/partners/pending")
    @Operation(summary = "승인 대기 파트너 목록 조회", description = "승인 대기 중인 파트너 회원 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class)))
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<List<MemberResponse>>> getPendingPartners() {
        log.info("승인 대기 중인 파트너 목록 조회 요청");
        
        // TODO: 실제로는 MemberRepository에서 승인 대기 중인 파트너들을 조회해야 함
        // 현재는 간단히 빈 리스트 반환
        List<MemberResponse> pendingPartners = List.of();
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("승인 대기 중인 파트너 목록 조회가 완료되었습니다.", pendingPartners));
    }

    /**
     * 파트너 승인 처리
     */
    @PostMapping("/partners/{partnerId}/approve")
    @Operation(summary = "파트너 승인", description = "대기 중인 파트너의 가입을 승인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "승인 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 처리된 파트너 등)"),
        @ApiResponse(responseCode = "404", description = "파트너를 찾을 수 없음")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> approvePartner(
            @Parameter(description = "파트너 ID", required = true) @PathVariable Long partnerId) {
        log.info("파트너 승인 요청 - 파트너: {}", partnerId);
        
        MemberResponse response = adminService.approvePartner(partnerId);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("파트너 승인이 완료되었습니다.", response));
    }

    /**
     * 파트너 승인 거부
     */
    @PostMapping("/partners/{partnerId}/reject")
    @Operation(summary = "파트너 승인 거부", description = "대기 중인 파트너의 가입을 거부합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "거부 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (거부 사유 누락 등)"),
        @ApiResponse(responseCode = "404", description = "파트너를 찾을 수 없음")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> rejectPartner(
            @Parameter(description = "파트너 ID", required = true) @PathVariable Long partnerId,
            @RequestBody 
            @Schema(description = "승인 거부 사유", example = """
                {
                    "reason": "사업자등록증이 불분명하여 승인이 어렵습니다. 다시 제출해 주세요."
                }
                """) 
            Map<String, String> request) {
        log.info("파트너 거부 요청 - 파트너: {}", partnerId);
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(com.sinsaflower.server.global.dto.ApiResponse.badRequest("거부 사유는 필수입니다."));
        }
        
        MemberResponse response = adminService.rejectPartner(partnerId, reason);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("파트너 승인 거부가 완료되었습니다.", response));
    }

    /**
     * 초기 관리자 생성 (개발용)
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
} 
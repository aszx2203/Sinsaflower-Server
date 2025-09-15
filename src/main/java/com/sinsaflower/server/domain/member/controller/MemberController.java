package com.sinsaflower.server.domain.member.controller;

import com.sinsaflower.server.domain.member.constants.MemberConstants;
import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.dto.MemberSignupRequest;
import com.sinsaflower.server.domain.member.entity.Member.MemberStatus;
import com.sinsaflower.server.domain.member.service.MemberService;
import com.sinsaflower.server.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "파트너 회원 관리", description = "파트너 회원가입 및 정보 관리 API")
public class MemberController {

    private final MemberService memberService;

    /**
     * 파트너 회원가입
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "파트너 회원가입", 
        description = "새로운 파트너 회원을 등록합니다. multipart/form-data 형식으로 JSON 데이터와 파일을 함께 전송합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공",
                     content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "중복된 정보")
    })
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> signup(
        @RequestPart("request") 
        @Schema(description = "회원가입 정보 (JSON)", implementation = MemberSignupRequest.class)
        @Valid MemberSignupRequest request,
        
        @RequestPart(value = "businessCertFile", required = false)
        @Schema(description = "사업자등록증 파일 (PDF, JPG, PNG)", type = "string", format = "binary")
        MultipartFile businessCertFile,
        
        @RequestPart(value = "bankCertFile", required = false)
        @Schema(description = "통장사본 파일 (PDF, JPG, PNG)", type = "string", format = "binary")
        MultipartFile bankCertFile
    ) {
        log.info("파트너 회원가입 요청: {}", request.getLoginId());

        if (request.getBusinessProfile() != null) {
            request.getBusinessProfile().setBusinessCertFile(businessCertFile);
            request.getBusinessProfile().setBankCertFile(bankCertFile);
        }

        MemberResponse response = memberService.signUp(request);
        log.info("회원가입 성공: {} (ID: {})", response.getLoginId(), response.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(com.sinsaflower.server.global.dto.ApiResponse.created(MemberConstants.Messages.SIGNUP_SUCCESS, response));
    }

    /**
     * 내 정보 조회
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", 
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"code\":404,\"message\":\"회원을 찾을 수 없습니다.\",\"timestamp\":\"2024-01-15T15:30:00\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("내 정보 조회 요청: {} (ID: {})", currentUser.getUsername(), currentUser.getUserId());
        
        MemberResponse response = memberService.getMemberInfo(currentUser.getUserId());
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(MemberConstants.Messages.MEMBER_INFO_RETRIEVED, response));
    }

    /**
     * 화환명으로 회원 검색
     */
    @GetMapping("/search/name")
    @Operation(summary = "화환명으로 회원 검색", description = "화환명(name)으로 회원을 검색합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Page<MemberResponse>>> searchByName(
            @RequestParam String name,
            Pageable pageable) {
        log.info("화환명 검색 요청: {}", name);
        
        Page<MemberResponse> response = memberService.searchMembersByName(name, pageable);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(MemberConstants.Messages.MEMBER_LIST_RETRIEVED, response));
    }

    /**
     * 지역별 회원 검색
     */
    @GetMapping("/search/region")
    @Operation(summary = "지역별 회원 검색", description = "활동 지역으로 회원을 검색합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Page<MemberResponse>>> searchByRegion(
            @RequestParam String sido,
            @RequestParam(required = false) String sigungu,
            @RequestParam(required = false) String eupmyeondong,
            Pageable pageable) {
        log.info("지역 검색 요청: {} {} {}", sido, sigungu, eupmyeondong);
        
        Page<MemberResponse> response = memberService.searchMembersByRegion(sido, sigungu, eupmyeondong, pageable);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(MemberConstants.Messages.MEMBER_LIST_RETRIEVED, response));
    }

    /**
     * 취급 상품별 회원 검색
     */
    @GetMapping("/search/product")
    @Operation(summary = "취급 상품별 회원 검색", description = "취급하는 상품으로 회원을 검색합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Page<MemberResponse>>> searchByProduct(
            @RequestParam String productName,
            Pageable pageable) {
        log.info("취급 상품 검색 요청: {}", productName);
        
        Page<MemberResponse> response = memberService.searchMembersByProduct(productName, pageable);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(MemberConstants.Messages.MEMBER_LIST_RETRIEVED, response));
    }

    /**
     * 복합 검색 (화환명 + 지역)
     */
    @GetMapping("/search/combined")
    @Operation(summary = "복합 검색", description = "화환명과 지역을 조합하여 회원을 검색합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Page<MemberResponse>>> searchCombined(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) String sigungu,
            @RequestParam(required = false) String productName,
            Pageable pageable) {
        log.info("복합 검색 요청: name={}, sido={}, sigungu={}, product={}", name, sido, sigungu, productName);
        
        Page<MemberResponse> response = memberService.searchMembersCombined(name, sido, sigungu, productName, pageable);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(MemberConstants.Messages.MEMBER_LIST_RETRIEVED, response));
    }

    /**
     * 비밀번호 변경
     */
    @PatchMapping("/me/password")
    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 회원이 비밀번호를 변경합니다.")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Void>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("비밀번호 변경 요청: {}", currentUser.getUsername());
        
        memberService.changePassword(currentUser.getUserId(), currentPassword, newPassword);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(MemberConstants.Messages.PASSWORD_UPDATED));
    }
} 
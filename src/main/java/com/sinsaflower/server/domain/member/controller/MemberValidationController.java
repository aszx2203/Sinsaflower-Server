package com.sinsaflower.server.domain.member.controller;

import com.sinsaflower.server.domain.member.dto.DuplicateCheckResponse;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import com.sinsaflower.server.domain.member.repository.MemberBusinessProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 정보 중복 확인 컨트롤러
 * 회원가입 시 필요한 각종 정보의 중복 여부를 확인하는 API 제공
 */
@Tag(name = "중복 확인 API", description = "회원가입 시 로그인 ID, 전화번호, 사업자등록번호 등의 중복 여부를 확인하는 API")
@RestController
@RequestMapping("/api/members/validation")
@RequiredArgsConstructor
@Slf4j
public class MemberValidationController {

    private final MemberRepository memberRepository;
    private final MemberBusinessProfileRepository memberBusinessProfileRepository;

    @Operation(
        summary = "로그인 ID 중복 확인", 
        description = "회원가입 시 로그인 ID의 중복 여부를 확인합니다. 이미 사용 중인 ID인지 실시간으로 확인할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "중복 확인 완료",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DuplicateCheckResponse.class,
                        example = "{\"code\":200,\"message\":\"중복 확인이 완료되었습니다.\",\"data\":{\"exists\":false,\"message\":\"사용 가능한 로그인 ID입니다.\",\"available\":true},\"timestamp\":\"2024-01-15T15:30:00\"}")
                ))
    })
    @GetMapping("/check-login-id/{loginId}")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<DuplicateCheckResponse>> checkLoginIdDuplicate(
            @Parameter(description = "확인할 로그인 ID", required = true, example = "partner123") 
            @PathVariable String loginId) {
        log.info("로그인 ID 중복 확인 요청: {}", loginId);
        
        boolean exists = memberRepository.existsByLoginId(loginId);
        DuplicateCheckResponse response = exists ? 
            DuplicateCheckResponse.loginIdDuplicate() : 
            DuplicateCheckResponse.loginIdAvailable();
            
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("중복 확인이 완료되었습니다.", response));
    }

    /**
     * 전화번호 중복 확인 (모든 회원 타입 공통)
     */
    @Operation(
        summary = "전화번호 중복 확인", 
        description = "회원가입 시 전화번호의 중복 여부를 확인합니다. 이미 등록된 전화번호인지 실시간으로 확인할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "중복 확인 완료",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DuplicateCheckResponse.class,
                        example = "{\"code\":200,\"message\":\"중복 확인이 완료되었습니다.\",\"data\":{\"exists\":false,\"message\":\"사용 가능한 전화번호입니다.\",\"available\":true},\"timestamp\":\"2024-01-15T15:30:00\"}")
                ))
    })
    @GetMapping("/check-mobile/{mobile}")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<DuplicateCheckResponse>> checkMobileDuplicate(
            @Parameter(description = "확인할 전화번호", required = true, example = "010-1234-5678") 
            @PathVariable String mobile) {
        log.info("전화번호 중복 확인 요청: {}", mobile);
        
        boolean exists = memberRepository.existsByMobile(mobile);
        DuplicateCheckResponse response = exists ? 
            DuplicateCheckResponse.mobileDuplicate() : 
            DuplicateCheckResponse.mobileAvailable();
            
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("중복 확인이 완료되었습니다.", response));
    }

    /**
     * 사업자등록번호 중복 확인 (파트너 회원용)
     */
    @Operation(
        summary = "사업자등록번호 중복 확인", 
        description = "파트너 회원가입 시 사업자등록번호의 중복 여부를 확인합니다. 이미 등록된 사업자등록번호인지 실시간으로 확인할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "중복 확인 완료",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DuplicateCheckResponse.class,
                        example = "{\"code\":200,\"message\":\"중복 확인이 완료되었습니다.\",\"data\":{\"exists\":false,\"message\":\"사용 가능한 사업자등록번호입니다.\",\"available\":true},\"timestamp\":\"2024-01-15T15:30:00\"}")
                ))
    })
    @GetMapping("/check-business-number/{businessNumber}")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<DuplicateCheckResponse>> checkBusinessNumberDuplicate(
            @Parameter(description = "확인할 사업자등록번호", required = true, example = "123-45-67890") 
            @PathVariable String businessNumber) {
        log.info("사업자등록번호 중복 확인 요청: {}", businessNumber);
        
        boolean exists = memberBusinessProfileRepository.existsByBusinessNumber(businessNumber);
        DuplicateCheckResponse response = exists ? 
            DuplicateCheckResponse.businessNumberDuplicate() : 
            DuplicateCheckResponse.businessNumberAvailable();
            
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("중복 확인이 완료되었습니다.", response));
    }


} 
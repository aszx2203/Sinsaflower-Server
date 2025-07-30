package com.sinsaflower.server.domain.member.controller;

import com.sinsaflower.server.domain.member.dto.MemberResponse;
import com.sinsaflower.server.domain.member.service.MemberService;
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

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "파트너 회원 관리", description = "로그인 후 파트너 회원 정보 관리 API")
public class MemberController {

    private final MemberService memberService;



    @Operation(summary = "회원 정보 조회", description = "회원 ID로 회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", 
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"code\":404,\"message\":\"회원을 찾을 수 없습니다. ID: 123\",\"timestamp\":\"2024-01-15T15:30:00\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/{memberId}")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> getMemberInfo(
            @Parameter(description = "회원 ID", required = true, example = "123") @PathVariable Long memberId) {
        log.info("회원 정보 조회 요청: {}", memberId);
        
        MemberResponse response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 정보 조회가 성공적으로 완료되었습니다.", response));
    }

    @Operation(summary = "로그인 ID로 회원 조회", description = "로그인 ID로 회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"code\":404,\"message\":\"회원을 찾을 수 없습니다. 로그인 ID: user123\",\"timestamp\":\"2024-01-15T15:30:00\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/login-id/{loginId}")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<MemberResponse>> getMemberByLoginId(
            @Parameter(description = "로그인 ID", required = true, example = "partner123") @PathVariable String loginId) {
        log.info("로그인 ID로 회원 조회 요청: {}", loginId);
        
        MemberResponse response = memberService.getMemberByLoginId(loginId);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success("회원 정보 조회가 성공적으로 완료되었습니다.", response));
    }


} 
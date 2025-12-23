package com.sinsaflower.server.domain.member.controller;

import com.sinsaflower.server.domain.member.constants.MemberConstants;
import com.sinsaflower.server.domain.member.dto.MemberRegionPriceRequest;
import com.sinsaflower.server.domain.member.dto.MemberRegionPriceResponse;
import com.sinsaflower.server.domain.member.service.MemberRegionPriceService;
import com.sinsaflower.server.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberRegionPriceController {

    private final MemberRegionPriceService service;

    @PostMapping("/me/regions-prices")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<Void>>  saveMyRegionsAndPrices(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody List<MemberRegionPriceRequest> requests
    ) {
        service.save(currentUser.getUserId(), requests);
        return ResponseEntity.ok(com.sinsaflower.server.global.dto.ApiResponse.success(MemberConstants.Messages.DELIVERY_REGION_UPLOADED));
    }

    @GetMapping("/me/regions-prices")
    public ResponseEntity<com.sinsaflower.server.global.dto.ApiResponse<List<MemberRegionPriceResponse>>>
    getMyRegionsAndPrices(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<MemberRegionPriceResponse> result =
                service.getMyRegionsAndPrices(currentUser.getUserId());

        return ResponseEntity.ok(
                com.sinsaflower.server.global.dto.ApiResponse.success(result)
        );
    }
}

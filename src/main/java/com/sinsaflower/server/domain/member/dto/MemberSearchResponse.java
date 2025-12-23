package com.sinsaflower.server.domain.member.dto;

import com.sinsaflower.server.domain.member.entity.MemberRank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MemberSearchResponse {
    private Long id;
    private String name;
    private String phone;
    private String region;

    private String memo;
    private List<String> tags = new ArrayList<>();

    private MemberRank rank;

    // 🔥 기존 DTO 재사용
    private List<MemberProductPriceResponse> prices = new ArrayList<>();
}

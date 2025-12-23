package com.sinsaflower.server.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class MemberRegionPriceRequest {
    private String sido;
    private String sigungu;
    private Boolean handled; // 지역 취급 여부
    private List<MemberProductPriceRequest> prices;
}

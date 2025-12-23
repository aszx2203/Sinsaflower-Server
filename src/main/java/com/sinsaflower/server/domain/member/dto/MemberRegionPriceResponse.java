package com.sinsaflower.server.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberRegionPriceResponse {

    private String sido;
    private String sigungu;
    private Boolean handled;
    private List<MemberProductPriceResponse> prices;
}
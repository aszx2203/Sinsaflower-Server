package com.sinsaflower.server.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MemberProductPriceResponse {

    private String categoryName;
    private BigDecimal price;       // 천원 단위
    private Boolean isAvailable;
}

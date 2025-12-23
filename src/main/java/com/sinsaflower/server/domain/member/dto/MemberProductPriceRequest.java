package com.sinsaflower.server.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberProductPriceRequest {
    private String categoryName; // 축하, 근조, 동양 ...
    private Integer price;       // 47 = 47,000원
    private Boolean isAvailable; // 명시적 취급 여부
}

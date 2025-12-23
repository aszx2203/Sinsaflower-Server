package com.sinsaflower.server.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MemberProductPriceDto {
    private final Long memberId;
    private final String categoryName;
    private final BigDecimal price;
    private final Boolean isAvailable;

    public MemberProductPriceDto(
            Long memberId,
            String categoryName,
            BigDecimal price,
            Boolean isAvailable
    ) {
        this.memberId = memberId;
        this.categoryName = categoryName;
        this.price = price;
        this.isAvailable = isAvailable;
    }
}

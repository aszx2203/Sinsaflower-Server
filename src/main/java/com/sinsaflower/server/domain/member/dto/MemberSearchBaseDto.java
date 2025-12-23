package com.sinsaflower.server.domain.member.dto;

import com.sinsaflower.server.domain.member.entity.MemberRank;
import lombok.Getter;

@Getter
public class MemberSearchBaseDto {

    private final Long memberId;
    private final String name;
    private final String phone;
    private final String region;
    private final String memo;
    private final MemberRank rank;

    public MemberSearchBaseDto(
            Long memberId,
            String name,
            String phone,
            String region,
            String memo,
            MemberRank rank
    ) {
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
        this.region = region;
        this.memo = memo;
        this.rank = rank;
    }
}

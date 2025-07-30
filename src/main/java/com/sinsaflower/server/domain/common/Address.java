package com.sinsaflower.server.domain.common;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    private String sido;           // 시/도: 서울특별시
    private String sigungu;        // 시/군/구: 강남구
    private String eupmyeondong;   // 읍/면/동: 역삼동
    private String detail;         // 상세주소: 테헤란로 123
    private String zipcode;        // 우편번호
}
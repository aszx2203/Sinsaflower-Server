package com.sinsaflower.server.domain.delivery.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region", indexes = {
    @Index(name = "idx_region_sido_sigungu", columnList = "sido, sigungu")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String sido; // 시/도: 강원, 서울, 경기 등

    @Column(length = 50, nullable = false)
    private String sigungu; // 시/군/구: 춘천시, 화천군 등

    @Column(length = 50)
    private String eupmyeondong; // 읍/면/동 (필요시)

    @Column(length = 10, nullable = false)
    private String zipcode; // 우편번호 앞 3자리

    @Column(nullable = false)
    private Boolean isActive = true;

    // 표시용 전체 지역명
    public String getFullName() {
        return sido + " " + sigungu;
    }

    // 배송 가능 지역인지 확인
    public boolean isDeliverable() {
        return isActive;
    }
} 
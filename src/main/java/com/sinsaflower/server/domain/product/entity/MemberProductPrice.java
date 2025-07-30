package com.sinsaflower.server.domain.product.entity;

import com.sinsaflower.server.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_product_price", 
       indexes = {
           @Index(name = "idx_member_region_category", columnList = "member_id, sido, sigungu, category_name")
       },
       uniqueConstraints = @UniqueConstraint(
           name = "uk_member_region_category",
           columnNames = {"member_id", "sido", "sigungu", "category_name"}
       ))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원

    @Column(length = 50, nullable = false)
    private String sido; // 시/도 (강원, 서울특별시, 경기도)

    @Column(length = 50, nullable = false)
    private String sigungu; // 시/군/구 (춘천시, 강남구, 남양주시)

    @Column(name = "category_name", length = 50, nullable = false)
    private String categoryName; // 상품 카테고리명 (축하, 근조, 동양, 서양, 꽃, 관엽, 쌀, 기타, 과일)

    @Column(precision = 10, scale = 0, nullable = false)
    private BigDecimal price; // 화환 가격 (천원 단위, 47 = 47,000원)

    @Column(nullable = false)
    private Boolean isAvailable = true; // 취급 가능 여부 (미취급 = false)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public boolean isHandling() {
        return isAvailable;
    }

    // 천원 단위 가격을 실제 원 단위로 변환
    public BigDecimal getPriceInWon() {
        return price.multiply(new BigDecimal("1000"));
    }

    // 표시용 가격 정보
    public String getPriceDisplay() {
        if (!isAvailable) {
            return "미취급";
        }
        return price.toString();
    }

    // 지역 전체 이름 반환
    public String getRegionFullName() {
        return sido + " " + sigungu;
    }
} 
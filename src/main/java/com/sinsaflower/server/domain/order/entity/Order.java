package com.sinsaflower.server.domain.order.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sinsaflower.server.domain.common.Address;
import com.sinsaflower.server.domain.common.BaseTimeEntity;
import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.product.entity.Product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_number", columnList = "orderNumber"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_member_id", columnList = "member_id"),
    @Index(name = "idx_delivery_date", columnList = "deliveryDate"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_member_status", columnList = "member_id, status"),
    @Index(name = "idx_delivery_date_status", columnList = "deliveryDate, status")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // orderId

    @Column(length = 50, nullable = false, unique = true)
    private String orderNumber; // 주문번호

    // Member와의 관계 추가 (필수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 주문한 회원 (화환업체)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 (주문당 상품 1개 고정)

    // 옵션 상품들 (1:N 관계)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderOption> orderOptions = new ArrayList<>();

    @Column(nullable = false)
    private Integer quantity; // 수량

    @Column(nullable = true)
    private Integer basePrice; // 원천 금액

    @Column(nullable = true)
    private Integer paymentPrice; // 결제(옵션제외) 금액

    @Column(nullable = true)
    private Integer optionPrice; // 옵션 상품 금액

    @Column(nullable = false)
    private Integer totalPrice; // 총 금액

    // 주문 상태 추가
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING; // 주문 상태

    @Column(length = 500)
    private String productFilePath; // 상품 이미지 경로

    // 주문자 정보 (Member 정보와 중복될 수 있지만 주문 시점 정보 보존)
    @Column(length = 50, nullable = false)
    private String ordererName; // 주문자 이름

    @Column(length = 20, nullable = true)
    private String ordererNumber; // 주문자 전화번호

    @Column(length = 20, nullable = true)
    private String ordererMobile; // 주문자 휴대전화번호

    // 수령인 정보
    @Column(length = 50, nullable = false)
    private String receiverName; // 수령인 이름

    @Column(length = 20, nullable = true)
    private String receiverNumber; // 수령인 전화번호

    @Column(length = 20, nullable = false)
    private String receiverMobile; // 수령인 휴대전화번호

    @Column(nullable = false)
    private LocalDate deliveryDate; // 배송 일자

    @Column(nullable = false)
    private LocalDateTime deliveryTime; // 배송 시간

    @Column(length = 10, nullable = false)
    private String deliveryDay; // 배송요일

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "sido", column = @Column(name = "delivery_sido")),
        @AttributeOverride(name = "sigungu", column = @Column(name = "delivery_sigungu")),
        @AttributeOverride(name = "eupmyeondong", column = @Column(name = "delivery_eupmyeondong")),
        @AttributeOverride(name = "detail", column = @Column(name = "delivery_detail")),
        @AttributeOverride(name = "zipcode", column = @Column(name = "delivery_zipcode"))
    })
    private Address deliveryAddress; // 배송 주소

    @Column(length = 100, nullable = true)
    private String occasion; // 경조사명 (변수명 수정)

    @Column(length = 50, nullable = true)
    private String fromName; // 보내는 사람 이름

    @Column(length = 500, nullable = true)
    private String cardMessage; // 카드 메시지

    @Column(length = 500, nullable = true)
    private String request; // 요구사항

    // 주문 상태 enum
    public enum OrderStatus {
        UNCHECKED("미확인"),
        PENDING("주문접수"),
        PREPARE("배송준비"),
        DELIVERED("배송완료");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}

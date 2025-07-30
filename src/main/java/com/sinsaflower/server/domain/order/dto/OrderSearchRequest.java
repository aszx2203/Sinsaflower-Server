package com.sinsaflower.server.domain.order.dto;

import com.sinsaflower.server.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@Schema(description = "발주리스트 검색 요청", example = """
    {
        "deliveryDateFrom": "2025-07-01",
        "deliveryDateTo": "2025-07-31",
        "status": "PENDING",
        "ordererName": "김화환",
        "receiverName": "홍길동",
        "deliverySido": "서울특별시",
        "deliverySigungu": "강남구",
        "orderNumber": "ORD-20250725",
        "memberId": 1,
        "page": 0,
        "size": 20,
        "sort": "createdAt,desc"
    }
    """)
public class OrderSearchRequest {

    @Schema(description = "배송 시작일 (검색 범위)", example = "2025-07-01")
    private LocalDate deliveryDateFrom;

    @Schema(description = "배송 종료일 (검색 범위)", example = "2025-07-31")
    private LocalDate deliveryDateTo;

    @Schema(description = "주문 상태", example = "PENDING",
            allowableValues = {"UNCHECKED", "PENDING", "PREPARE", "DELIVERED"})
    private Order.OrderStatus status;

    @Schema(description = "주문자명 (부분 검색)", example = "김화환")
    private String ordererName;

    @Schema(description = "수령인명 (부분 검색)", example = "홍길동")
    private String receiverName;

    @Schema(description = "배송지 시/도", example = "서울특별시")
    private String deliverySido;

    @Schema(description = "배송지 시/군/구", example = "강남구")
    private String deliverySigungu;

    @Schema(description = "주문번호 (부분 검색)", example = "ORD-20250725")
    private String orderNumber;

    @Schema(description = "회원 ID (특정 회원의 주문만 조회)", example = "1")
    private Long memberId;
    // 페이지네이션
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "정렬 조건", example = "createdAt,desc", defaultValue = "createdAt,desc",
            allowableValues = {
                "createdAt,desc", "createdAt,asc",
                "deliveryDate,desc", "deliveryDate,asc", 
                "totalPrice,desc", "totalPrice,asc",
                "orderNumber,desc", "orderNumber,asc"
            })
    private String sort = "createdAt,desc";

    // 편의 메서드
    public boolean hasDeliveryDateRange() {
        return deliveryDateFrom != null || deliveryDateTo != null;
    }

    public boolean hasLocationFilter() {
        return deliverySido != null || deliverySigungu != null;
    }
}
package com.sinsaflower.server.domain.order.dto;

import com.sinsaflower.server.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "발주리스트 페이지 응답")
public class OrderListResponse {

    @Schema(description = "주문 목록")
    private List<OrderSummary> orders;

    @Schema(description = "페이지 정보")
    private PageInfo pageInfo;

    @Schema(description = "검색 결과 통계")
    private SearchStatistics statistics;

    @Getter
    @Builder
    @Schema(description = "주문 요약 정보 (발주리스트용)")
    public static class OrderSummary {
        @Schema(description = "주문 ID", example = "1")
        private Long id;

        @Schema(description = "주문번호", example = "ORD-20250725-001")
        private String orderNumber;

        @Schema(description = "주문 생성일시", example = "2025-07-25T11:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "배송 요구일", example = "2025-07-25")
        private LocalDate deliveryDate;

        @Schema(description = "배송 요일", example = "금요일")
        private String deliveryDay;

        @Schema(description = "주문자", example = "김화환")
        private String ordererName;

        @Schema(description = "수령인", example = "홍길동")
        private String receiverName;

        @Schema(description = "회원 정보")
        private MemberSummary member;

        @Schema(description = "상품 정보")
        private ProductSummary product;

        @Schema(description = "배송지 요약", example = "서울특별시 강남구 역삼동")
        private String deliveryLocationSummary;

        @Schema(description = "원천 금액", example = "50000")
        private Integer basePrice;

        @Schema(description = "총 결제 금액", example = "95000")
        private Integer totalPrice;

        @Schema(description = "주문 상태", example = "PENDING")
        private Order.OrderStatus status;

        @Schema(description = "주문 상태 설명", example = "주문접수")
        private String statusDescription;

        @Schema(description = "팩스번호", example = "02-123-4567")
        private String faxNumber;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String contactNumber;

        @Schema(description = "경조사명", example = "개업축하")
        private String occasion;

        @Schema(description = "옵션 요약", example = "케이크, 와인")
        private String optionsSummary;
    }

    @Getter
    @Builder
    @Schema(description = "회원 요약 정보")
    public static class MemberSummary {
        @Schema(description = "회원 ID", example = "1")
        private Long id;

        @Schema(description = "화환업체명", example = "신사꽃농장")
        private String name;

        @Schema(description = "닉네임", example = "신사꽃")
        private String nickname;

        @Schema(description = "휴대전화번호", example = "010-1234-5678")
        private String mobile;
    }

    @Getter
    @Builder
    @Schema(description = "상품 요약 정보")
    public static class ProductSummary {
        @Schema(description = "상품 ID", example = "1")
        private Long id;

        @Schema(description = "상품명", example = "개업축하화환")
        private String productName;

        @Schema(description = "상품 코드", example = "1001")
        private Integer productCode;
    }

    @Getter
    @Builder
    @Schema(description = "페이지 정보")
    public static class PageInfo {
        @Schema(description = "현재 페이지 번호", example = "0")
        private Integer currentPage;

        @Schema(description = "페이지 크기", example = "20")
        private Integer pageSize;

        @Schema(description = "전체 요소 수", example = "150")
        private Long totalElements;

        @Schema(description = "전체 페이지 수", example = "8")
        private Integer totalPages;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean isLast;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        private Boolean hasPrevious;
    }

    @Getter
    @Builder  
    @Schema(description = "검색 결과 통계")
    public static class SearchStatistics {
        @Schema(description = "전체 주문 건수", example = "150")
        private Long totalOrders;

        @Schema(description = "총 주문 금액", example = "14250000")
        private Long totalAmount;

        // @Schema(description = "평균 주문 금액", example = "95000")
        // private Integer averageAmount;
    }
}
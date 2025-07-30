package com.sinsaflower.server.domain.order.dto;

import com.sinsaflower.server.domain.order.entity.Order;
import com.sinsaflower.server.domain.order.entity.OrderOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "주문 상세 정보 응답")
public class OrderResponse {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "주문번호", example = "ORD-20250725-001")
    private String orderNumber;

    @Schema(description = "주문 상태", example = "PENDING")
    private Order.OrderStatus status;

    @Schema(description = "주문 상태 설명", example = "주문접수")
    private String statusDescription;

    @Schema(description = "상품 정보")
    private ProductInfo product;

    @Schema(description = "회원 정보")
    private MemberInfo member;

    @Schema(description = "주문 수량", example = "1")
    private Integer quantity;

    @Schema(description = "기본 상품 금액", example = "50000")
    private Integer basePrice;

    @Schema(description = "옵션 상품 총 금액", example = "45000")
    private Integer optionPrice;

    @Schema(description = "총 주문 금액", example = "95000")
    private Integer totalPrice;

    @Schema(description = "주문자 이름", example = "김화환")
    private String ordererName;

    @Schema(description = "주문자 전화번호", example = "02-123-4567")
    private String ordererNumber;

    @Schema(description = "주문자 휴대전화번호", example = "010-1234-5678")
    private String ordererMobile;

    @Schema(description = "수령인 이름", example = "홍길동")
    private String receiverName;

    @Schema(description = "수령인 전화번호", example = "02-987-6543")
    private String receiverNumber;

    @Schema(description = "수령인 휴대전화번호", example = "010-9876-5432")
    private String receiverMobile;

    @Schema(description = "배송 일자", example = "2025-07-25")
    private LocalDate deliveryDate;

    @Schema(description = "배송 시간", example = "2025-07-25T14:00:00")
    private LocalDateTime deliveryTime;

    @Schema(description = "배송 요일", example = "금요일")
    private String deliveryDay;

    @Schema(description = "배송 주소")
    private AddressInfo deliveryAddress;

    @Schema(description = "경조사명", example = "개업축하")
    private String occasion;

    @Schema(description = "보내는 분 이름", example = "신사꽃농장")
    private String fromName;

    @Schema(description = "카드 메시지", example = "개업을 진심으로 축하드립니다.")
    private String cardMessage;

    @Schema(description = "요구사항", example = "1층 로비에 배치 부탁드립니다")
    private String request;

    @Schema(description = "추가 옵션 목록")
    private List<OrderOptionInfo> orderOptions;

    @Schema(description = "주문 생성일시", example = "2025-07-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "주문 수정일시", example = "2025-07-20T15:45:00")
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    @Schema(description = "상품 정보")
    public static class ProductInfo {
        @Schema(description = "상품 ID", example = "1")
        private Long id;

        @Schema(description = "상품명", example = "개업축하화환")
        private String productName;

        @Schema(description = "상품 코드", example = "1001")
        private Integer productCode;
    }

    @Getter
    @Builder
    @Schema(description = "회원 정보")
    public static class MemberInfo {
        @Schema(description = "회원 ID", example = "1")
        private Long id;

        @Schema(description = "로그인 ID", example = "partner123")
        private String loginId;

        @Schema(description = "화환업체명", example = "신사꽃농장")
        private String name;

        @Schema(description = "닉네임", example = "신사꽃")
        private String nickname;

        @Schema(description = "휴대전화번호", example = "010-1234-5678")
        private String mobile;
    }

    @Getter
    @Builder
    @Schema(description = "주소 정보")
    public static class AddressInfo {
        @Schema(description = "시/도", example = "서울특별시")
        private String sido;

        @Schema(description = "시/군/구", example = "강남구")
        private String sigungu;

        @Schema(description = "읍/면/동", example = "역삼동")
        private String eupmyeondong;

        @Schema(description = "상세 주소", example = "123-45번지 테헤란로 427")
        private String detail;

        @Schema(description = "우편번호", example = "06142")
        private String zipcode;

        @Schema(description = "전체 주소", example = "서울특별시 강남구 역삼동 123-45번지 테헤란로 427")
        private String fullAddress;
    }

    @Getter
    @Builder
    @Schema(description = "주문 옵션 정보")
    public static class OrderOptionInfo {
        @Schema(description = "옵션 ID", example = "1")
        private Long id;

        @Schema(description = "옵션 종류", example = "CAKE")
        private OrderOption.OptionproductType optionName;

        @Schema(description = "옵션 종류 설명", example = "케이크")
        private String optionDescription;

        @Schema(description = "옵션 가격", example = "15000")
        private Integer price;
    }
}
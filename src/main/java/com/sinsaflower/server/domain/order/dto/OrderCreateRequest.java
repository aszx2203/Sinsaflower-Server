package com.sinsaflower.server.domain.order.dto;

import com.sinsaflower.server.domain.order.entity.OrderOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Schema(description = "주문 생성 요청", example = """
    {
        "productId": 1,
        "quantity": 1,
        "basePrice": 95000,
        "totalPrice": 95000,
        "ordererName": "김화환",
        "ordererNumber": "02-123-4567",
        "ordererMobile": "010-1234-5678",
        "receiverName": "홍길동",
        "receiverNumber": "02-987-6543",
        "receiverMobile": "010-9876-5432",
        "deliveryDate": "2025-07-25",
        "deliveryTime": "2025-07-25T14:00:00",
        "deliveryDay": "금요일",
        "deliveryAddress": {
            "sido": "서울특별시",
            "sigungu": "강남구",
            "eupmyeondong": "역삼동",
            "detail": "123-45번지 테헤란로 427",
            "zipcode": "06142"
        },
        "occasion": "개업축하",
        "fromName": "신사꽃농장",
        "cardMessage": "개업을 진심으로 축하드립니다.",
        "request": "1층 로비에 배치 부탁드립니다",
        "orderOptions": [
            {
                "optionName": "CAKE",
                "price": 15000
            },
            {
                "optionName": "WINE",
                "price": 30000
            }
        ]
    }
    """)
public class OrderCreateRequest {

    @Schema(description = "상품 ID", example = "1", required = true)
    private Long productId;

    @Schema(description = "주문 수량", example = "1", required = true)
    private Integer quantity;

    @Schema(description = "원천 금액", example = "95000")
    private Integer basePrice;

    @Schema(description = "결제(옵션제외) 금액", example = "15000")
    private Integer paymentPrice;

    @Schema(description = "총 주문 금액", example = "95000", required = true)
    private Integer totalPrice;

    @Schema(description = "주문자 이름", example = "김화환")
    private String ordererName;

    @Schema(description = "주문자 전화번호", example = "02-123-4567")
    private String ordererNumber;

    @Schema(description = "주문자 휴대전화번호", example = "010-1234-5678")
    private String ordererMobile;

    @Schema(description = "수령인 이름", example = "홍길동", required = true)
    private String receiverName;

    @Schema(description = "수령인 전화번호", example = "02-987-6543",  required = true)
    private String receiverNumber;

    @Schema(description = "수령인 휴대전화번호", example = "010-9876-5432", required = true)
    private String receiverMobile;

    @Schema(description = "배송 일자", example = "2025-07-25", required = true)
    private LocalDate deliveryDate;

    @Schema(description = "배송 시간", example = "2025-07-25T14:00:00", required = true)
    private LocalDateTime deliveryTime;

    @Schema(description = "배송 요일", example = "금요일", required = true)
    private String deliveryDay;

    @Schema(description = "배송 주소", required = true)
    private AddressRequest deliveryAddress;

    @Schema(description = "경조사명", example = "개업축하")
    private String occasion;

    @Schema(description = "보내는 분 이름", example = "신사꽃농장")
    private String fromName;

    @Schema(description = "카드 메시지", example = "개업을 진심으로 축하드립니다.")
    private String cardMessage;

    @Schema(description = "요구사항", example = "1층 로비에 배치 부탁드립니다")
    private String request;

    @Schema(description = "추가 옵션 목록")
    private List<OrderOptionRequest> orderOptions;

    @Getter @Setter
    @Schema(description = "주소 정보")
    public static class AddressRequest {
        @Schema(description = "시/도", example = "서울특별시", required = true)
        private String sido;

        @Schema(description = "시/군/구", example = "강남구", required = true)
        private String sigungu;

        @Schema(description = "읍/면/동", example = "역삼동")
        private String eupmyeondong;

        @Schema(description = "상세 주소", example = "123-45번지 테헤란로 427", required = true)
        private String detail;

        @Schema(description = "우편번호", example = "06142", required = true)
        private String zipcode;
    }

    @Getter @Setter
    @Schema(description = "주문 옵션 정보")
    public static class OrderOptionRequest {
        @Schema(description = "옵션 상품 종류", example = "CAKE",
                allowableValues = {"CAKE", "WINE", "CHAMPAGNE", "CHOCOLATE", "CANDY", "PEPERO", "OTHER", "POT", "RIBBON", "OCCASION", "DELIVERY"})
        private OrderOption.OptionproductType optionName;

        @Schema(description = "옵션 가격", example = "15000")
        private Integer price;
    }
}
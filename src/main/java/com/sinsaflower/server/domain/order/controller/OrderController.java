package com.sinsaflower.server.domain.order.controller;

import com.sinsaflower.server.domain.order.dto.*;
import com.sinsaflower.server.domain.order.entity.Order;
import com.sinsaflower.server.domain.order.service.OrderService;
import com.sinsaflower.server.global.dto.ApiResponse;
import com.sinsaflower.server.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "주문 관리", description = "화환 주문 생성, 조회, 발주리스트 관리 API")
public class OrderController {

    private final OrderService orderService;

    @Operation(
        summary = "주문 생성",
        description = """
            새로운 화환 주문을 생성합니다.
            
            **주요 기능:**
            - 회원별 주문 생성
            - 상품 정보 및 옵션 설정  
            - 배송 정보 등록
            - 자동 주문번호 생성 (ORD-YYYYMMDD-001 형식)
            
            **주문 상태:**
            - 생성 시 기본 상태: UNCHECKED (미확인)
            
            **옵션 상품 종류:**
            - CAKE: 케이크
            - WINE: 와인  
            - CHAMPAGNE: 샴페인
            - CHOCOLATE: 초콜릿
            - CANDY: 사탕
            - PEPERO: 빼빼로
            - OTHER: 기타
            - POT: 화분받침대
            - RIBBON: 리본교체비
            - OCCASION: 경조사비
            - DELIVERY: 배송비
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "주문 생성 정보",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderCreateRequest.class),
                examples = @ExampleObject(
                    name = "주문 생성 예시",
                    value = """
                        {
                          "productId": 1,
                          "quantity": 1,
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
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "주문 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                        {
                          "success": true,
                          "message": "주문이 성공적으로 생성되었습니다.",
                          "data": {
                            "id": 1,
                            "orderNumber": "ORD-20250725-001",
                            "createdAt": "2025-07-25T10:30:00"
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                        {
                          "success": false,
                          "message": "필수 정보가 누락되었습니다.",
                          "errors": ["배송 주소는 필수입니다.", "수령인 연락처는 필수입니다."]
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "회원 또는 상품을 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증이 필요합니다."
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody OrderCreateRequest request
    ) {
        log.info("주문 생성 요청: memberId={}, productId={}, totalPrice={}", 
            userDetails.getUserId(), request.getProductId(), request.getTotalPrice());

            OrderCreateResponse response = orderService.createOrder(userDetails.getUserId(), request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("주문이 성공적으로 생성되었습니다.", response));
    }

    @Operation(
        summary = "주문 상태 변경",
        description = """
            주문의 상태를 변경합니다.
            
            **상태 전환 흐름:**
            1. UNCHECKED (미확인) → PENDING (주문접수)
            2. PENDING (주문접수) → PREPARE (배송준비)  
            3. PREPARE (배송준비) → DELIVERED (배송완료)
            
            **권한:**
            - 관리자 또는 해당 주문의 회원만 변경 가능
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "주문 상태 변경 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 상태 변경 요청"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증이 필요합니다."
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "권한이 없습니다."
        )
    })
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
        @Parameter(description = "주문 ID", example = "1")
        @PathVariable Long orderId,
        
        @Parameter(description = "변경할 주문 상태", example = "PENDING", 
                   schema = @Schema(allowableValues = {"UNCHECKED", "PENDING", "PREPARE", "DELIVERED"}))
        @RequestParam Order.OrderStatus status
    ) {
        log.info("주문 상태 변경: orderId={}, newStatus={}", orderId, status);

        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        
        return ResponseEntity.ok(ApiResponse.success("주문 상태가 변경되었습니다.", response));
    }
}
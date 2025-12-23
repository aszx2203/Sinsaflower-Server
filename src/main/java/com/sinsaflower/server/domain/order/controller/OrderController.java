package com.sinsaflower.server.domain.order.controller;

import com.sinsaflower.server.domain.order.dto.OrderCreateRequest;
import com.sinsaflower.server.domain.order.dto.OrderCreateResponse;
import com.sinsaflower.server.domain.order.dto.OrderResponse;
import com.sinsaflower.server.domain.order.entity.Order;
import com.sinsaflower.server.domain.order.entity.Order.OrderStatus;
import com.sinsaflower.server.domain.order.service.OrderService;
import com.sinsaflower.server.domain.order.constants.OrderConstants;
import com.sinsaflower.server.domain.delivery.entity.Region;
import com.sinsaflower.server.domain.delivery.repository.RegionRepository;
import com.sinsaflower.server.global.dto.ApiResponse;
import com.sinsaflower.server.global.security.CustomUserDetails;
import com.sinsaflower.server.global.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "주문 관리", description = "주문 생성, 수정, 삭제 API")
public class OrderController {

    private final OrderService orderService;
    private final RegionRepository regionRepository;

    /**
     * 주문 생성 (JSON 데이터 + 이미지 파일)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. JSON 데이터와 이미지 파일을 함께 전송할 수 있습니다.")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @Parameter(description = "주문 데이터 (JSON)", required = true)
            @RequestPart("orderData") @Valid OrderCreateRequest request,
            
            @Parameter(description = "상품 이미지 파일", required = false)
            @RequestPart(value = "productImage", required = false) MultipartFile productImage,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {

                
        log.info("Creating order for member: {}", userDetails.getUserId());

        // DTO -> Entity 변환
        Order orderData = request.toEntity();
        
        // 지역 설정 (regionId가 있는 경우)
        if (request.getRegionId() != null) {
            Region region = regionRepository.findById(request.getRegionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Region not found: " + request.getRegionId()));
            orderData.setRegion(region);
        }
        
        // 연관 엔티티들 설정
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            request.getOptions().forEach(optionReq -> {
                orderData.addOrderOption(optionReq.toEntity());
            });
        }
        
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            request.getMessages().forEach(messageReq -> {
                orderData.addOrderMessage(messageReq.toEntity());
            });
        }
        
        if (request.getSenders() != null && !request.getSenders().isEmpty()) {
            request.getSenders().forEach(senderReq -> {
                orderData.addOrderSender(senderReq.toEntity());
            });
        }

        // 주문 생성 (이미지 포함)
        Order savedOrder;
        if (productImage != null && !productImage.isEmpty()) {
            savedOrder = orderService.createOrderWithImage(userDetails.getUserId(), orderData, productImage);
        } else {
            savedOrder = orderService.createOrder(userDetails.getUserId(), orderData);
        }

        OrderCreateResponse response = OrderCreateResponse.from(savedOrder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(OrderConstants.Messages.ORDER_CREATED, response));
    }

//    /**
//     * 주문 조회
//     */
//    @GetMapping("/{orderId}")
//    @Operation(summary = "주문 조회", description = "주문 ID로 특정 주문을 조회합니다.")
//    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
//            @PathVariable Long orderId,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("Getting order: {} by member: {}", orderId, userDetails.getUserId());
//
//        Order order = orderService.getOrder(orderId);
//        OrderResponse response = OrderResponse.from(order);
//
//        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_RETRIEVED, response));
//    }

    /**
     * 내 주문 목록 조회
     */
//    @GetMapping("/my")
//    @Operation(summary = "내 주문 목록 조회", description = "로그인한 회원의 주문 목록을 조회합니다.")
//    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "createdAt") String sort,
//            @RequestParam(defaultValue = "desc") String direction,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("Getting orders for member: {}", userDetails.getUserId());
//
//        Pageable pageable = PagingUtils.createPageable(page, size, sort, direction);
//        Page<Order> orders = orderService.getOrdersByMember(userDetails.getUserId(), pageable);
//        Page<OrderResponse> response = orders.map(OrderResponse::from);
//
//        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_LIST_RETRIEVED, response));
//    }

    /**
     * 주문 상태별 조회 (관리자용)
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "주문 상태별 조회", description = "특정 상태의 주문들을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Getting orders by status: {}", status);

        Pageable pageable = PagingUtils.createPageable(page, size, sort, direction);
        Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
        Page<OrderResponse> response = orders.map(OrderResponse::from);

        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_LIST_RETRIEVED, response));
    }
//
//    /**
//     * 내 주문 상태별 조회 (일반 사용자용)
//     */
//    @GetMapping("/my/status/{status}")
//    @Operation(summary = "내 주문 상태별 조회", description = "로그인한 회원의 특정 상태 주문들을 조회합니다.")
//    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrdersByStatus(
//            @PathVariable OrderStatus status,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "createdAt") String sort,
//            @RequestParam(defaultValue = "desc") String direction,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("Getting my orders by status: {} for member: {}", status, userDetails.getUserId());
//
//        Pageable pageable = PagingUtils.createPageable(page, size, sort, direction);
//        Page<Order> orders = orderService.getOrdersByMemberAndStatus(userDetails.getUserId(), status, pageable);
//        Page<OrderResponse> response = orders.map(OrderResponse::from);
//
//        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_LIST_RETRIEVED, response));
//    }

    /**
     * 배송일별 주문 조회
     */
    @GetMapping("/delivery-date/{date}")
    @Operation(summary = "배송일별 주문 조회", description = "특정 배송일의 주문들을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByDeliveryDate(
            @PathVariable LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Getting orders by delivery date: {}", date);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Order> orders = orderService.getOrdersByDeliveryDate(date, pageable);
        Page<OrderResponse> response = orders.map(OrderResponse::from);

        return ResponseEntity.ok(ApiResponse.success("배송일별 주문 조회 완료", response));
    }

    /**
     * 주문 상태 변경
     */
    @PatchMapping("/{orderId}/status")
    @Operation(summary = "주문 상태 변경", description = "주문의 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, OrderStatus> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderStatus newStatus = request.get("orderStatus");
        log.info("Updating order status: {} to {} by member: {}", orderId, newStatus, userDetails.getUserId());

        Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        OrderResponse response = OrderResponse.from(updatedOrder);

        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_STATUS_UPDATED, response));
    }

    /**
     * 주문 수정
     */
    @PutMapping("/{orderId}")
    @Operation(summary = "주문 수정", description = "주문 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Updating order: {} by member: {}", orderId, userDetails.getUserId());

        Order updateData = request.toEntity();
        Order updatedOrder = orderService.updateOrder(orderId, updateData);
        OrderResponse response = OrderResponse.from(updatedOrder);

        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_UPDATED, response));
    }

    /**
     * 상품 이미지 업로드
     */
    @PostMapping("/{orderId}/image")
    @Operation(summary = "상품 이미지 업로드", description = "주문에 상품 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> uploadProductImage(
            @PathVariable Long orderId,
            @RequestParam("image") MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Uploading product image for order: {} by member: {}", orderId, userDetails.getUserId());

        Order updatedOrder = orderService.uploadProductImage(orderId, imageFile);
        OrderResponse response = OrderResponse.from(updatedOrder);

        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.IMAGE_UPLOADED, response));
    }

    /**
     * 상품 이미지 삭제
     */
    @DeleteMapping("/{orderId}/image")
    @Operation(summary = "상품 이미지 삭제", description = "주문의 상품 이미지를 삭제합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> deleteProductImage(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Deleting product image for order: {} by member: {}", orderId, userDetails.getUserId());

        Order updatedOrder = orderService.deleteProductImage(orderId);
        OrderResponse response = OrderResponse.from(updatedOrder);

        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.IMAGE_DELETED, response));
    }

    /**
     * 주문 삭제
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 삭제", description = "주문을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Deleting order: {} by member: {}", orderId, userDetails.getUserId());

        orderService.deleteOrder(orderId, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_DELETED, null));
    }

    /**
     * 오늘 주문 목록 조회
     */
    @GetMapping("/today")
    @Operation(summary = "오늘 주문 조회", description = "오늘 등록된 주문들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getTodayOrders() {
        log.info("Getting today's orders");

        List<Order> orders = orderService.getTodayOrders();
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("오늘 주문 조회 완료", response));
    }

    /**
     * 오늘 배송 예정 주문 조회
     */
    @GetMapping("/today-delivery")
    @Operation(summary = "오늘 배송 예정 주문 조회", description = "오늘 배송 예정인 주문들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getTodayDeliveryOrders() {
        log.info("Getting today's delivery orders");

        List<Order> orders = orderService.getTodayDeliveryOrders();
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("오늘 배송 예정 주문 조회 완료", response));
    }

    /**
     * 주문 통계 조회
     */
    @GetMapping("/statistics")
    @Operation(summary = "주문 통계 조회", description = "주문 상태별 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getOrderStatistics() {
        log.info("Getting order statistics");

        Map<String, Long> statistics = orderService.getOrderStatistics();

        return ResponseEntity.ok(ApiResponse.success("주문 통계 조회 완료", statistics));
    }
//
//    /**
//     * 발주 리스트 조회 (이미지 API 명세 기반)
//     */
//    @GetMapping("/purchase")
//    @Operation(summary = "발주 리스트 조회", description = "발주 리스트를 조회합니다. 날짜 범위, 검색 조건 등을 지원합니다.")
//    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getPurchaseOrders(
//            @Parameter(description = "검색 시작일 (yyyy-MM-dd)")
//            @RequestParam(required = false) String startDate,
//
//            @Parameter(description = "검색 종료일 (yyyy-MM-dd)")
//            @RequestParam(required = false) String endDate,
//
//            @Parameter(description = "날짜 필드 타입 (createdAt, orderDate, deliveryDate)")
//            @RequestParam(defaultValue = "createdAt") String dateField,
//
//            @Parameter(description = "주문 상태 필터")
//            @RequestParam(required = false) OrderStatus orderStatus,
//
//            @Parameter(description = "검색 필드 (purchaseShopName, productName 등)")
//            @RequestParam(required = false) String searchField,
//
//            @Parameter(description = "검색 키워드")
//            @RequestParam(required = false) String searchKeyword,
//
//            @Parameter(description = "페이지 번호 (0부터 시작)")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "페이지 크기")
//            @RequestParam(defaultValue = "20") int size,
//
//            @Parameter(description = "정렬 필드")
//            @RequestParam(defaultValue = "createdAt") String sort,
//
//            @Parameter(description = "정렬 방향 (asc, desc)")
//            @RequestParam(defaultValue = "desc") String direction,
//
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("Getting purchase orders with search conditions");
//
//        // 검색 조건 객체 생성
//        OrderSearchRequest searchRequest = OrderSearchRequest.builder()
//                .startDate(parseDate(startDate))
//                .endDate(parseDate(endDate))
//                .dateField(dateField)
//                .orderStatus(orderStatus)
//                .searchField(searchField)
//                .searchKeyword(searchKeyword)
//                .page(page)
//                .size(size)
//                .sort(sort)
//                .direction(direction)
//                .build();
//
//        // 유효성 검증
//        if (!searchRequest.isValidDateRange()) {
//            return ResponseEntity.badRequest()
//                    .body(ApiResponse.error(OrderConstants.Messages.INVALID_DATE_RANGE));
//        }
//
//        if (!searchRequest.isValidSearchField()) {
//            return ResponseEntity.badRequest()
//                    .body(ApiResponse.error(OrderConstants.Messages.INVALID_SEARCH_FIELD));
//        }
//
//        // 페이징 설정
//        Pageable pageable = PagingUtils.createPageable(page, size, sort, direction);
//
//        // 주문 목록 조회 (본인 주문만 조회)
//        Page<Order> orders = orderService.searchOrders(
//                List.of(userDetails.getUserId()), // 본인 memberId만 포함
//                orderStatus,
//                searchRequest.getStartDate(),
//                searchRequest.getEndDate(),
//                null, // regionIds
//                pageable
//        );
//
//        // OrderListResponse로 변환
//        Page<OrderListResponse> response = orders.map(OrderListResponse::from);
//
//        return ResponseEntity.ok(ApiResponse.success("발주 리스트 조회 완료", response));
//    }

    /**
     * 발주 요약 통계 조회 (이미지 API 명세 기반)
     */
//    @GetMapping("/purchase/summary")
//    @Operation(summary = "발주 요약 통계 조회", description = "본인의 발주 요약 통계를 조회합니다.")
//    public ResponseEntity<ApiResponse<OrderSummaryResponse>> getPurchaseSummary(
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("Getting purchase summary for member: {}", userDetails.getUserId());
//
//        OrderSummaryResponse summary = orderService.getOrderSummary(userDetails.getUserId());
//
//        return ResponseEntity.ok(ApiResponse.success("발주 요약 통계 조회 완료", summary));
//    }

    /**
     * 발주서/영수증 조회 (이미지 API 명세 기반)
     */
//    @GetMapping("/purchase/{orderId}/receipt")
//    @Operation(summary = "발주서/영수증 조회", description = "특정 주문의 발주서/영수증 상세 정보를 조회합니다.")
//    public ResponseEntity<ApiResponse<OrderResponse>> getPurchaseReceipt(
//            @PathVariable Long orderId,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        log.info("Getting purchase receipt for order: {} by member: {}", orderId, userDetails.getUserId());
//
//        Order order = orderService.getOrder(orderId);
//
//        // 본인 주문인지 확인 (보안)
//        if (!order.getMember().getId().equals(userDetails.getUserId())) {
//            throw new ResourceNotFoundException("Order not found or access denied: " + orderId);
//        }
//
//        OrderResponse response = OrderResponse.from(order);
//
//        return ResponseEntity.ok(ApiResponse.success(OrderConstants.Messages.ORDER_RETRIEVED, response));
//    }

    /**
     * 문자열을 LocalDate로 변환하는 헬퍼 메서드
     */
    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.warn("Invalid date format: {}", dateStr);
            return null;
        }
    }
}

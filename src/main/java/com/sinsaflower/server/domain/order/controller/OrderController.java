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

}

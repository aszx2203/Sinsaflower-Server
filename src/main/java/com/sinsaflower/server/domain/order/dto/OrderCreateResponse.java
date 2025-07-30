package com.sinsaflower.server.domain.order.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "주문 생성 응답")
public class OrderCreateResponse {
    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "주문번호", example = "ORD-20250725-001")
    private String orderNumber;

    @Schema(description = "주문 생성일시", example = "2025-07-20T10:30:00")
    private LocalDateTime createdAt;

}

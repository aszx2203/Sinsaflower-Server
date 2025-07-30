package com.sinsaflower.server.domain.order.service;

import com.sinsaflower.server.domain.member.entity.Member;
import com.sinsaflower.server.domain.member.repository.MemberRepository;
import com.sinsaflower.server.domain.order.dto.*;
import com.sinsaflower.server.domain.order.entity.Order;
import com.sinsaflower.server.domain.order.entity.OrderOption;
import com.sinsaflower.server.domain.order.repository.OrderRepository;
import com.sinsaflower.server.domain.product.entity.Product;

import com.sinsaflower.server.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다."));

        // 주문번호 생성
        String orderNumber = generateOrderNumber();

        // 주문 생성
        Order order = Order.builder()
            .orderNumber(orderNumber)
            .member(member)
            .quantity(request.getQuantity())
            .totalPrice(request.getTotalPrice())
            .status(Order.OrderStatus.UNCHECKED)
            .ordererName(request.getOrdererName())
            .ordererNumber(request.getOrdererNumber())
            .ordererMobile(request.getOrdererMobile())
            .receiverName(request.getReceiverName())
            .receiverNumber(request.getReceiverNumber())
            .receiverMobile(request.getReceiverMobile())
            .deliveryDate(request.getDeliveryDate())
            .deliveryTime(request.getDeliveryTime())
            .deliveryDay(request.getDeliveryDay())
            .occasion(request.getOccasion())
            .fromName(request.getFromName())
            .cardMessage(request.getCardMessage())
            .request(request.getRequest())
            .build();

        // 배송 주소 설정
        if (request.getDeliveryAddress() != null) {
            order.setDeliveryAddress(convertToAddress(request.getDeliveryAddress()));
        }

        // 옵션 상품 추가
        if (request.getOrderOptions() != null && !request.getOrderOptions().isEmpty()) {
            List<OrderOption> orderOptions = request.getOrderOptions().stream()
                .map(optionReq -> OrderOption.builder()
                    .optionName(optionReq.getOptionName())
                    .price(optionReq.getPrice())
                    .build())
                .collect(Collectors.toList());
            order.setOrderOptions(orderOptions);
        }

        Order savedOrder = orderRepository.save(order);
        
        log.info("주문 생성 완료: 주문번호={}, 회원ID={}, 총액={}", 
            savedOrder.getOrderNumber(), memberId, savedOrder.getTotalPrice());

        return convertToOrderCreateResponse(savedOrder);
    }

    /**
     * 주문 상세 조회
     */
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        
        return convertToOrderResponse(order);
    }

    /**
     * 주문 상태 변경
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        log.info("주문 상태 변경: 주문번호={}, 상태={}", savedOrder.getOrderNumber(), newStatus);

        return convertToOrderResponse(savedOrder);
    }

    // 내부 메서드들

    /**
     * 주문번호 생성 (YYYYMMDD-001 형식)
     */
    private String generateOrderNumber() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = today + "-";
        
        Long todayOrderCount = orderRepository.countTodayOrdersByPrefix(prefix);
        String sequence = String.format("%03d", todayOrderCount + 1);
        
        return prefix + sequence;
    }

    /**
     * 페이지네이션 객체 생성
     */
    private Pageable createPageable(OrderSearchRequest request) {
        Sort sort = parseSort(request.getSort());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * 정렬 조건 파싱
     */
    private Sort parseSort(String sortString) {
        if (sortString == null || sortString.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sortString.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String property = parts[0].trim();
        String direction = parts[1].trim().toLowerCase();
        
        Sort.Direction sortDirection = "asc".equals(direction) ? 
            Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(sortDirection, property);
    }

    private OrderCreateResponse convertToOrderCreateResponse(Order order) {
        return OrderCreateResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .createdAt(order.getCreatedAt())
            .build();
    }

    /**
     * Order -> OrderResponse 변환
     */
    private OrderResponse convertToOrderResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus())
            .statusDescription(order.getStatus().getDescription())
            .member(convertToMemberInfo(order.getMember()))
            .product(convertToProductInfo(order.getProduct()))
            .quantity(order.getQuantity())
            .basePrice(order.getBasePrice())
            .optionPrice(order.getOptionPrice())
            .totalPrice(order.getTotalPrice())
            .ordererName(order.getOrdererName())
            .ordererNumber(order.getOrdererNumber())
            .ordererMobile(order.getOrdererMobile())
            .receiverName(order.getReceiverName())
            .receiverNumber(order.getReceiverNumber())
            .receiverMobile(order.getReceiverMobile())
            .deliveryDate(order.getDeliveryDate())
            .deliveryTime(order.getDeliveryTime())
            .deliveryDay(order.getDeliveryDay())
            .occasion(order.getOccasion())
            .fromName(order.getFromName())
            .cardMessage(order.getCardMessage())
            .request(order.getRequest())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    // 기타 변환 메서드들
    private com.sinsaflower.server.domain.common.Address convertToAddress(OrderCreateRequest.AddressRequest request) {
        com.sinsaflower.server.domain.common.Address address = new com.sinsaflower.server.domain.common.Address();
        address.setSido(request.getSido());
        address.setSigungu(request.getSigungu());
        address.setEupmyeondong(request.getEupmyeondong());
        address.setDetail(request.getDetail());
        address.setZipcode(request.getZipcode());
        return address;
    }

    private OrderResponse.MemberInfo convertToMemberInfo(Member member) {
        return OrderResponse.MemberInfo.builder()
            .id(member.getId())
            .loginId(member.getLoginId())
            .name(member.getName())
            .nickname(member.getNickname())
            .mobile(member.getMobile())
            .build();
    }

    private OrderResponse.ProductInfo convertToProductInfo(Product product) {
        if (product == null) return null;
        
        return OrderResponse.ProductInfo.builder()
            .id(product.getId())
            .productName(product.getProductName())
            .productCode(product.getProductCode())
            .build();
    }

}
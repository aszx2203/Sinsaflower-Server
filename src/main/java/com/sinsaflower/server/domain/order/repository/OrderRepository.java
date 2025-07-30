package com.sinsaflower.server.domain.order.repository;

import com.sinsaflower.server.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 주문번호로 주문 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 회원별 주문 목록 조회 (최신순)
     */
    Page<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    /**
     * 특정 상태의 주문 목록 조회
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);

    /**
     * 배송일자별 주문 목록 조회
     */
    Page<Order> findByDeliveryDateOrderByDeliveryTime(LocalDate deliveryDate, Pageable pageable);

    /**
     * 특정 기간 내 주문 목록 조회 (배송일 기준)
     */
    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.member
        JOIN FETCH o.product
        WHERE o.deliveryDate BETWEEN :startDate AND :endDate
        AND o.isDeleted = false
        ORDER BY o.deliveryDate ASC, o.deliveryTime ASC
        """)
    List<Order> findOrdersByDeliveryDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );


    /**
     * 주문번호 중복 확인
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * 최근 주문번호 생성을 위한 오늘 주문 개수 조회
     */
    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE DATE(o.createdAt) = CURRENT_DATE
        AND o.orderNumber LIKE :orderNumberPrefix%
        """)
    Long countTodayOrdersByPrefix(@Param("orderNumberPrefix") String orderNumberPrefix);
}
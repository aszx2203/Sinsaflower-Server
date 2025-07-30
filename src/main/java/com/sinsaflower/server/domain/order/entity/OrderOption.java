package com.sinsaflower.server.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_option", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId; // Order와의 외래키 (JoinColumn으로 관리됨)

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private OptionproductType optionName; // 옵션명 (케이크, 와인, 리본교체 등)

    @Column(nullable = false)
    private Integer price; // 옵션 가격

    public enum OptionproductType {

        CAKE("케이크"), 
        WINE("와인"),
        CHAMPAGNE("샴페인"),
        CHOCOLATE("초콜릿"),
        CANDY("사탕"),
        PEPERO("빼빼로"),
        OTHER("기타"),
        POT("화분받침대"),
        RIBBON("리본교체비"),
        OCCASION("경조사비"),
        DELIVERY("배송비");

        private final String description;
        
        OptionproductType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }

    }

}
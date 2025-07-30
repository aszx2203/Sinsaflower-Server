package com.sinsaflower.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // productId

    @Column(length = 50, nullable = false)
    private String productName; // 상품명

    @Column(length = 10, nullable = false)
    private Integer productCode; // 상품코드

    @Column(length = 10, nullable = false)
    private Integer sortNumber; // 정렬 번호

    @Column(nullable = false)
    private Boolean isActive; // 활성 여부


}
